(ns cljss.collect
  (:require [cljss.utils :refer [build-css]]))

(defn dynamic? [[_ value]]
  (not (or (string? value)
           (number? value))))

(defn varid [cls idx [rule]]
  [rule (str "--var-" cls "-" idx)])

(defn collect-styles [cls styles rule-index]
  (let [dynamic (filterv dynamic? styles)
        static  (filterv (comp not dynamic?) styles)
        [vars rule-index] (reduce
                            (fn [[vars idx] ds]
                              (let [ret (conj vars (varid cls idx ds))]
                                [ret (inc idx)]))
                            [[] rule-index]
                            dynamic)
        _ (println "static" static)
        _ (println "vars" vars)
        _ (println "dynamic" dynamic)
        vals    (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        _ (println "vals" vals)
        static  (->> vars
                     (map (fn [[rule var]] [rule (str "var(" var ")")]))
                     (concat static)
                     (build-css cls))]
    [static vals rule-index]))

(defn collect-dynamic-styles [rule-index rules class-generator]
  (loop
    [idx rule-index
     acc []
     coll rules]
    (if (not (seq coll))
      [acc idx]
      (let [[rule styles] (first coll)
            [static vals nxt-idx] (collect-styles (class-generator rule) styles idx)]
        (recur nxt-idx
               (conj acc [static vals])
               (rest coll))))))
