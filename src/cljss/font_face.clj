(ns cljss.font-face
  (:require [clojure.string :as cstr]))

(defn- compile-src [m]
  (->> m
       (map (fn [[k v]] [(name k) "(\"" v "\")"]))
       (interpose " ")))

(defmulti compile-font-face (fn [[k v]] k))

(defmethod compile-font-face :default [[k v]]
  [(name k) v])

(defmethod compile-font-face :font-family [[k v]]
  [(name k)
   (if (string? v)
     (str "\"" v "\"")
     ["\"" v "\""])])

(defmethod compile-font-face :unicode-range [[k v]]
  [(name k) (interpose ", " v)])

(defmethod compile-font-face :src [[k v]]
  [(name k) (->> v (map compile-src) (interpose ", "))])

(defn- literal? [x]
  (or (string? x) (number? x)))

(defn- reduce-str [s]
  (->> s
       (reduce
         (fn [s s1]
           (let [s0 (first s)
                 srest (rest s)]
             (if (and (literal? s1) (string? s0))
               (cons (str s0 s1) srest)
               (cons s1 s))))
         (list ""))
       reverse))

(defn font-face [descriptors]
  (let [s (->> descriptors
               (map compile-font-face)
               (map (fn [[rule val]] [(str (name rule) ":") val ";"]))
               flatten)]
    (if (every? literal? s)
      (str "@font-face{" (apply str s) "}")
      `(str "@font-face{" ~@(reduce-str s) "}"))))
