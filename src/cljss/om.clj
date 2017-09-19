(ns cljss.om
  (:require [cljss.core :refer [->styled]]))

(defmacro defstyled [var tag styles]
  (let [[tag# id# atomic# static# empty?# vals# attrs#] (->styled tag styles)
        create-element# `#(apply js/React.createElement ~tag# (cljs.core/clj->js %1) %2)]
    `(def ~var
       (cljss.om/styled ~id# ~atomic# ~static# ~empty?# ~vals# ~attrs# ~create-element#))))
