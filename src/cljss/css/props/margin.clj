(ns cljss.css.props.margin
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]))

(defmulti margin count)

(defmethod margin 1 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[v]]
        {:top    v
         :left   v
         :right  v
         :bottom v}))))

(defmethod margin 2 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[vertical horizontal]]
        {:top    vertical
         :left   horizontal
         :right  horizontal
         :bottom vertical}))))

(defmethod margin 3 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[top horizontal bottom]]
        {:top    top
         :left   horizontal
         :right  horizontal
         :bottom bottom}))))

(defmethod margin 4 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[top left right bottom]]
        {:top    top
         :left   left
         :right  right
         :bottom bottom}))))

(s/def ::margin
  (s/multi-spec margin count))

(s/def ::top
  (s/cat
    :top ::units/dimension))

(s/def ::right
  (s/cat
    :right ::units/dimension))

(s/def ::bottom
  (s/cat
    :bottom ::units/dimension))

(s/def ::left
  (s/cat
    :left ::units/dimension))

(comment
  (s/conform ::margin [[0 :px] [100 :cm] [1.5 :em] [1 :px]])
  (s/conform ::top [16 :px]))
