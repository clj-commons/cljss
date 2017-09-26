(ns cljss.media
  (:require [clojure.string :as cstr]))

;; https://github.com/noprompt/garden/blob/3b499270357ffbb27f83feadb2215984f3061e27/src/garden/compiler.cljc#L490-L518

(defn media? [form]
  (and (list? form)
       (->> form first (= 'at-media))))

(defn- space-separated-list [xs]
  (cstr/join " " xs))

(defn- comma-separated-list [xs]
  (let [ys (for [x xs]
             (if (sequential? x)
               (space-separated-list x)
               x))]
    (cstr/join ", " ys)))

(defn- render-media-expr-part [[k v]]
  (cond
    (true? v) (name k)
    (false? v) (str "not " (name k))
    (= :only v) (str "only " (name k))
    :else (if (and v (string? v))
            (str "(" (name k) ": " v ")")
            (str "(" (name k) ")"))))

(defn render-media-expr [expr]
  (println (sequential? expr))
  (if (sequential? expr)
    (->> (map render-media-expr expr)
         (comma-separated-list))
    (->> (map render-media-expr-part expr)
         (cstr/join " and "))))

(defn render-media-block [media-expr styles]
  (str "@media " (render-media-expr media-expr)
       "{" styles "}"))

(comment
  (at-media {:screen true}
            {:width "320px"}))

(comment
  (at-media [{:screen true} {:print false}]
            {:width "320px"}))
