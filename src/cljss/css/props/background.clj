(ns cljss.css.props.background
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]
            [cljss.css.colors :as colors]))

(s/def ::color
  (s/or
    :value ::colors/color
    :keyword #{:inherit :transparent}))

(s/def ::image
  (s/or
    :value string?
    :keyword #{:inherit :none}))

(s/def ::repeat
  #{:repeat :repeat-x :repeat-y :no-repeat :inherit})

(s/def ::attachment
  #{:scroll :fixed :inherit})

(s/def ::position
  some?)

(s/def ::background
  (s/or
    :keyword #{:inherit}
    :value (s/cat
             :color (s/? ::color)
             :image (s/? ::image)
             :repeat (s/? ::repeat)
             :attachment (s/? ::attachment)
             :position (s/? ::position))))

(s/conform ::background [[:hex "000"]])
