(ns cljss.collect
  (:require [cljss.utils :refer [build-css]]))

(defn dynamic? [[_ value]]
  (not (or (string? value)
           (number? value))))

(defn varid [cls idx [rule]]
  [rule (str "--var-" cls "-" idx)])

(defn collect-styles [cls styles]
  (let [dynamic (filterv dynamic? styles)
        static  (filterv (comp not dynamic?) styles)
        [vars _]
                (reduce
                  (fn [[vars idx] ds]
                    (let [ret (conj vars (varid cls idx ds))]
                      [ret (inc idx)]))
                  [[] 0]
                  dynamic)
        vals    (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        static  (->> vars
                     (map (fn [[rule var]] [rule (str "var(" var ")")]))
                     (concat static)
                     (build-css cls))]
    [static vals]))
