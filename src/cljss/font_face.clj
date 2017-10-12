(ns cljss.font-face
  (:require [clojure.string :as cstr]))

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

(defn- compile-css-rule [[rule val]]
  (let [r [(str (name rule) ":")]
        r (if (vector? val)
            (into r val)
            (conj r val))]
    (conj r ";")))

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
               (mapcat compile-css-rule))]
    (if (every? literal? s)
      (str "@font-face{" (apply str s) "}")
      `(cljs.core/str "@font-face{" ~@(reduce-str s) "}"))))
