(ns cljss.css.props.position
  (:require [clojure.spec.alpha :as s]))

(s/def ::position
  #{:static :relative :absolute :fixed :inherit})



(defmulti compile-css identity)

(defmethod compile-css :default [value]
  (name value))
