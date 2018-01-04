(ns cljss.css.props.position
  (:require [clojure.spec.alpha :as s]))

(s/def ::position
  #{:static :relative :absolute :fixed :inherit})
