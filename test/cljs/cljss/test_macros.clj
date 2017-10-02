(ns cljss.test-macros
  (:require [cljss.core :refer [->styled var->cls-name]]))

(defmacro test-styled [var tag styles]
  (let [cls-name# (var->cls-name var)]
    (->styled tag styles cls-name#)))
