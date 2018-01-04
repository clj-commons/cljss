(ns cljss.css.props.color
  (:require [clojure.spec.alpha :as s]
            [cljss.css.colors :as colors]
            [cljss.css.units :as units]))

(s/def ::color
  (s/or
    :value ::colors/color
    :keyword #{:inherit}))


(defmulti compile-css first)

(defmethod compile-css :default [value]
  (units/compile-css value))

(defmethod compile-css :value [[_ value]]
  (colors/compile-css value))
