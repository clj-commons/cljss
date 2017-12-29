(ns cljss.collect
  (:require [cljss.utils :refer [build-css]]))

(def env* (atom {:id  0
                 :cls nil}))

(defn reset-env! [v]
  (reset! env* (merge {:id 0 :cls nil} v)))

(defn dynamic? [[_ value]]
  (not (or (string? value)
           (number? value))))

(defn varid [cls idx [rule]]
  [rule (str "--var-" cls "-" idx)])

(defn collect-styles [cls styles]
  (let [id      (:cls @env*)
        dynamic (filterv dynamic? styles)
        static  (filterv (comp not dynamic?) styles)
        vars
                (reduce
                  (fn [vars ds]
                    (let [ret (conj vars (varid id (:id @env*) ds))]
                      (swap! env* update :id inc)
                      ret))
                  []
                  dynamic)
        vals    (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        static  (->> vars
                     (map (fn [[rule var]] [rule (str "var(" var ")")]))
                     (concat static)
                     (build-css cls))]
    [static vals]))
