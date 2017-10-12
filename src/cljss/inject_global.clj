(ns cljss.inject-global
  (:require [cljss.utils :refer [literal? compile-css-rule reduce-str]]))

(defn- compile-css [css]
  (mapcat compile-css-rule css))

(defn inject-global [sheet]
  (->> sheet
       (map (fn [[s css]]
              [(name s) (compile-css css)]))
       (map (fn [[s css]]
              [s
               (if (every? literal? css)
                 (str s "{" (apply str css) "}")
                 `(cljs.core/str ~s "{" ~@(reduce-str css) "}"))]))))
