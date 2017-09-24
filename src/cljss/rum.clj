(ns cljss.rum
  (:require [cljss.core :refer [->styled var->cls-name]]))

(defmacro defstyled [var tag styles]
  (let [cls-name# (var->cls-name var)
        [tag# static# vals# attrs#] (->styled tag styles cls-name#)
        create-element# `#(apply js/React.createElement ~tag# (cljs.core/clj->js %1) (sablono.core/html %2))]
    `(def ~var
       (cljss.rum/styled ~cls-name# ~static# ~vals# ~attrs# ~create-element#))))
