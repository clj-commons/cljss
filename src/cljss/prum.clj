(ns cljss.prum
  (:require [cljss.core :refer [->styled var->cls-name sym->cmp-name]]))

(defmacro defstyled [var tag styles]
  (let [cls-name#       (var->cls-name var)
        cmp-name#       (sym->cmp-name var)
        [tag# static# vals# attrs#] (->styled tag styles cls-name#)
        create-element# `#(apply sablono.preact/createElement ~tag# (cljs.core/clj->js %1) %2)]
    `(do
       (def ~var
         (cljss.prum/styled ~cls-name# ~static# ~vals# ~attrs# ~create-element#))
       (set! ~var ~'-displayName ~cmp-name#))))
