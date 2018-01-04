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
  (s/conform ::top [100 :px]))
