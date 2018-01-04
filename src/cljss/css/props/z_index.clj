(ns cljss.css.props.z-index
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]))

(s/def ::z-index
  (s/or
    :numeric ::units/integer
    :keyword #{:auto :inherit}))
