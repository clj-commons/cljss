(ns cljss.reagent
  (:require [cljss.core :refer [->styled var->cls-name]]))

(defmacro defstyled [var tag styles]
  (let [cls-name# (var->cls-name var)
        [tag# static# vals# attrs#] (->styled tag styles cls-name#)
        create-element# `#(apply js/React.createElement ~tag# (cljs.core/clj->js %1) (map reagent.core/as-element %2))]
    `(def ~var
       (cljss.reagent/styled ~cls-name# ~static# ~vals# ~attrs# ~create-element#))))
