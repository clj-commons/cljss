(ns cljss.css.props.z-index
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]))

(s/def ::z-index
  (s/or
    :numeric ::units/integer
    :keyword #{:auto :inherit}))



(defmulti compile-css first)

(defmethod compile-css :numeric [[_ value]]
  (units/compile-css [:numeric {:value value}]))

(defmethod compile-css :keyword [value]
  (units/compile-css value))
