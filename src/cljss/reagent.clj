(ns cljss.reagent
  (:require [cljss.core :refer [->styled]]))

(defmacro defstyled [var tag styles]
  (let [tag# tag
        [id# static# vals# attrs#] (->styled styles)
        create-element# `#(apply vector ~tag# %1 %2)]
    `(def ~var
       (cljss.rum/styled ~id# ~static# ~vals# ~attrs# ~create-element#))))
