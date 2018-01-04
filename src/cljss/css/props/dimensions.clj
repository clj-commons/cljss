(ns cljss.css.props.dimensions
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]))

(s/def ::value
  (s/or
    :length ::units/length
    :percentage ::units/percentage
    :keyword #{:auto :inherit}))

(s/def ::value-min
  (s/or
    :length ::units/length
    :percentage ::units/percentage
    :keyword #{:inherit}))

(s/def ::value-max
  (s/or
    :length ::units/length
    :percentage ::units/percentage
    :keyword #{:none :inherit}))

(s/def ::width ::value)
(s/def ::min-width ::value-min)
(s/def ::max-width ::value-max)

(s/def ::height ::value)
(s/def ::min-height ::value-min)
(s/def ::max-height ::value-max)



(defmulti compile-css identity)

(defmethod compile-css :default [value]
  (units/compile-css value))
