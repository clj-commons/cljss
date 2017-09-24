(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert! filled?]]
            [cljss.utils :refer [build-css]]
            [clojure.string :as cstr]))

(defonce ^:private sheets (atom (list (create-sheet))))
(defonce ^:private *id* (atom 0))
(defonce ^:private cache (atom {}))

(defn css
  "Takes class name, static styles and dynamic styles.
   Injects styles and returns a string of generated class names."
  [cls static vars]
  (let [sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css cls static vars))
      (do
        (when-not (empty? static)
          (insert! sheet static cls))
        (if (pos? (count vars))
          (if-let [var-cls (get @cache vars)]
            (str cls " " var-cls)
            (let [var-cls (str "vars-" (swap! *id* inc))]
              (insert! sheet (build-css var-cls vars) var-cls)
              (swap! cache assoc vars var-cls)
              (str cls " " var-cls)))
          cls)))))

(defn css-keyframes
  "Takes CSS animation name, static styles and dynamic styles.
   Injects styles and returns generated CSS animation name."
  [static vars]
  (let [sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css-keyframes static vars))
      (let [inner
            (reduce
              (fn [s [id val]] (cstr/replace s id val))
              static
              vars)
            anim-name (str "animation-" (hash vars))
            keyframes (str "@keyframes " anim-name "{" inner "}")]
        (insert! sheet keyframes anim-name)
        anim-name))))
