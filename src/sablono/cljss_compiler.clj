(ns sablono.cljss-compiler
  (:require [sablono.compiler :as s]
            [sablono.util :as sutil]
            [cljss.core :as cljss]
            [cljss.utils :refer [empty-css?]]))

(defn- compile-class [class styles]
  (let [[id atomic static vals] (cljss/build-styles styles)
        gen-class `(cljss.core/css ~id ~atomic ~static ~vals ~(empty-css? static))]
    (if (seq class)
      `(apply str ~gen-class " " ~@(interpose " " class))
      gen-class)))

;; WARNING: NEVER EVER DO THIS AT HOME!
;; swap `sablono.compiler/compile-attrs` fn with own, adjusted, implementation
(alter-var-root
  #'s/compile-attrs
  (fn [_]
    (fn [{:keys [class className class-name] :as attrs}]
      (let [class (filter identity [class className class-name])]
        (->> (seq attrs)
             (reduce (fn [attrs [name value]]
                       (if (= name :css)
                         (assoc attrs :class (compile-class class value))
                         (assoc attrs name (s/compile-attr name value))))
                     nil)
             (sutil/html-to-dom-attrs)
             (s/to-js))))))
