(ns cljss.builder
  (:require [cljss.utils :refer [build-css]]
            [cljss.validation.css :as validator]
            [cuerdas.core :as c]))

(defn varid [cls idx [rule]]
  [rule (str "--var-" cls "-" idx)])

(defn pseudo? [[rule value]]
  (and (re-matches #"&:.*" (name rule))
       (map? value)))

(defn dynamic? [[_ value]]
  (not (or (string? value)
           (number? value))))

(defn status? [[rule value]]
  (and (re-matches #"^.*\?$" (name rule))
       (map? value)))

(defn collect-styles [id cls idx styles]
  (let [dynamic (filterv dynamic? styles)
        static  (filterv (comp not dynamic?) styles)
        [vars idx]
        (reduce
          (fn [[vars idx] ds]
            [(conj vars (varid id idx ds))
             (inc idx)])
          [[] idx]
          dynamic)
        vals    (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        static  (->> vars
                     (map (fn [[rule var]] [rule (str "var(" var ")")]))
                     (concat static)
                     (build-css cls))]
    [static vals idx]))

(defn build-styles [cls styles]
  (validator/validate-css-properties styles)
  (let [{:keys [error ast]} (->> styles validator/validate-styles (group-by (comp ffirst last)))
        styles (if (seq error)
                 (apply dissoc styles (map first error))
                 styles)
        styles (if (seq ast)
                 (apply assoc styles (mapcat validator/compile-styles ast))
                 styles)
        pseudo  (filterv pseudo? styles)
        styles  (filterv (comp not pseudo?) styles)
        [static vals idx] (collect-styles cls cls 0 styles)
        pstyles (->> pseudo
                     (reduce
                       (fn [[coll idx] [rule styles]]
                         [(conj coll (collect-styles cls (str cls (subs (name rule) 1)) idx styles))
                          (inc idx)])
                       [[] idx])
                     first)
        vals    (->> pstyles
                     (mapcat second)
                     (into vals))]
    (doseq [[_ {:keys [error]}] error]
      (println
        (c/<<-
          (c/istr "WARNING - Invalid CSS syntax
                  ~{error}"))))
    [cls
     (apply str static (map first pstyles))
     vals]))
