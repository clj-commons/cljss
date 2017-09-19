(ns cljss.reagent
  (:require [cljss.core :refer [->styled]]))

(defmacro defstyled [var tag styles]
  (let [[tag# id# atomic# static# empty?# vals# attrs#] (->styled tag styles)
        create-element# `#(apply js/React.createElement ~tag# (cljs.core/clj->js %1) (map reagent.core/as-element %2))]
    `(def ~var
       (cljss.reagent/styled ~id# ~atomic# ~static# ~empty?# ~vals# ~attrs# ~create-element#))))
