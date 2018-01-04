(ns cljss.css.props.padding
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]))

(defmulti padding count)

(defmethod padding 1 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[v]]
        {:top    v
         :left   v
         :right  v
         :bottom v}))))

(defmethod padding 2 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[vertical horizontal]]
        {:top    vertical
         :left   horizontal
         :right  horizontal
         :bottom vertical}))))

(defmethod padding 3 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[top horizontal bottom]]
        {:top    top
         :left   horizontal
         :right  horizontal
         :bottom bottom}))))

(defmethod padding 4 [_]
  (s/and
    (s/coll-of ::units/dimension)
    (s/conformer
      (fn [[top left right bottom]]
        {:top    top
         :left   left
         :right  right
         :bottom bottom}))))

(s/def ::padding
  (s/multi-spec padding count))

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
  (s/conform ::padding [[0 :px] [100 :cm] [1.5 :em] [1 :px]])
  (s/conform ::left [0 :px]))
