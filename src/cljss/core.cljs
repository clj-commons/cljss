(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert! filled?]]
            [cljss.utils :refer [build-css]]))

(defonce ^:private sheets (atom (list (create-sheet))))

(defn css
  "Takes class name, static styles and dynamic styles.
   Injects styles and returns a string of generated class names."
  [cls static vars]
  (let [css-cls (str "css-" cls)
        sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css cls static vars))
      (do
        (when-not (empty? static)
          (insert! sheet static css-cls))
        (if (pos? (count vars))
          (let [var-cls (str "vars-" (hash vars))]
            (insert! sheet (build-css var-cls vars) var-cls)
            (str css-cls " " var-cls))
          css-cls)))))
