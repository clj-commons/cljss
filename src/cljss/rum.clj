(ns cljss.rum
  (:require [cljss.core :refer [->styled -styled var->cls-name sym->cmp-name var->cmp-name]]
            [cljss.utils :refer [cljs-env?]]))

(defmacro defstyled [var tag styles]
  (let [cls-name#       (var->cls-name var)
        cmp-name#       (var->cmp-name var)
        [tag# static# vals# attrs#] (->styled tag styles cls-name# &env)
        create-element# `#(apply js/React.createElement ~tag# (cljs.core/clj->js %1) (sablono.core/html %2))]
    (if-not (cljs-env? &env)
      `(def ~var
         ~(-styled tag cls-name# static# vals# attrs#))
      `(do
         (def ~var
           (cljss.rum/styled ~cls-name# ~static# ~vals# ~attrs# ~create-element#))
         (set! ~var ~'-displayName ~cmp-name#)))))
