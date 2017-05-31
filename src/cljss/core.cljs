(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert!]]
            [cljss.utils :refer [build-css]]))

(defonce ^:private sheet (create-sheet))

(defn css [cls vars]
  (if (seq vars)
    (let [var-cls (str "vars-" cls)]
      (insert! sheet (build-css var-cls vars))
      (str "css-" cls " " var-cls))
    (str "css-" cls)))
