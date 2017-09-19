(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert! filled?]]
            [cljss.utils :refer [build-css]]
            [clojure.string :as cstr]))

(defonce ^:private sheets (atom (list (create-sheet))))

(defn insert-css! [css cls]
  (let [sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (insert-css! cls css))
      (insert! sheet css cls))))

(defn css
  "Takes class name, static styles and dynamic styles.
   Injects styles and returns a string of generated class names."
  [cls acls static vars empty-css?]
  (let [scls (when (not empty-css?) (str "css-" cls))
        aclss (when (seq acls) (cstr/join " " acls))
        css-cls (->> [scls aclss] (cstr/join " ") cstr/trim)]
    (when-not empty-css?
      (insert-css! static css-cls))
    (if (pos? (count vars))
      (let [var-cls (str "vars-" (hash vars))]
        (insert-css! (build-css var-cls vars) var-cls)
        (str css-cls " " var-cls))
      css-cls)))

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
        (insert-css! keyframes anim-name)
        anim-name))))
