(ns cljss.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::defstyled-args
  (s/cat :var symbol? :tag keyword? :styles map?))

(s/fdef cljss.core/defstyles
        :args (s/cat :var symbol? :args vector? :styles map?)
        :ret any?)

(s/fdef cljss.rum/defstyled
        :args ::defstyled-args
        :ret any?)

(s/fdef cljss.prum/defstyled
        :args ::defstyled-args
        :ret any?)

(s/fdef cljss.reagent/defstyled
        :args ::defstyled-args
        :ret any?)

(s/fdef cljss.om/defstyled
        :args ::defstyled-args
        :ret any?)

(s/fdef cljss.core/defkeyframes
        :args (s/cat :var symbol? :args vector? :keyframes map?)
        :ret any?)

(s/fdef cljss.core/font-face
        :args (s/cat :descriptors map?)
        :ret any?)

(s/fdef cljss.core/inject-global
        :args (s/cat :styles map?)
        :ret any?)
