(ns cljss.css.props.offset
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]))

(s/def ::value
  (s/or
    :length ::units/length
    :percentage ::units/percentage
    :keyword #{:auto :inherit}))

(s/def ::top ::value)

(s/def ::right ::value)

(s/def ::bottom ::value)

(s/def ::left ::value)

(comment
  (s/conform ::top :auto))


(defmulti compile-css first)

(defmethod compile-css :default [value]
  (units/compile-css value))
