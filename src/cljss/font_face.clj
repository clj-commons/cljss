(ns cljss.font-face
  (:require [cljss.utils :refer [literal? compile-css-rule reduce-str]]))

(defn- compile-src [m]
  (->> m
       (map (fn [[k v]] [(name k) "(\"" v "\")"]))
       (interpose " ")
       (mapcat identity)
       (into [])))

(defmulti compile-font-face (fn [[k v]] k))

(defmethod compile-font-face :default [[k v]]
  [(name k) v])

(defmethod compile-font-face :font-family [[k v]]
  [(name k)
   (if (string? v)
     (str "\"" v "\"")
     ["\"" v "\""])])

(defmethod compile-font-face :unicode-range [[k v]]
  [(name k)
   (->> v
        (interpose ", ")
        (into []))])

(defmethod compile-font-face :src [[k v]]
  [(name k)
   (->> v
        (map compile-src)
        (interpose ", ")
        (mapcat identity)
        (into []))])

(defn font-face [descriptors]
  (let [s (->> descriptors
               (map compile-font-face)
               (mapcat compile-css-rule))]
    (if (every? literal? s)
      (str "@font-face{" (apply str s) "}")
      `(cljs.core/str "@font-face{" ~@(reduce-str s) "}"))))
