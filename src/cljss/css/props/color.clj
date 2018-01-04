(ns cljss.css.props.color
  (:require [clojure.spec.alpha :as s]
            [cljss.css.colors :as colors]))

(s/def ::color
  (s/or
    :value ::colors/color
    :keyword #{:inherit}))
