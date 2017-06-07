(ns cljss.core
  (:require [cljsjs.react]
            [sablono.core :refer [html]]
            [cljss.sheet :refer [create-sheet insert!]]
            [cljss.utils :refer [build-css]]))

(defonce ^:private sheet (create-sheet))

(defn css [cls vars]
  (if (seq vars)
    (let [var-cls (str "vars-" (hash vars))]
      (insert! sheet (build-css var-cls vars))
      (str "css-" cls " " var-cls))
    (str "css-" cls)))

(defn styled [tag cls vars attrs]
  (fn [props & children]
    (let [[props children] (if (map? props) [props children] [{} (into [props] children)])
          varClass (->> vars (map (fn [[cls v]] (if (ifn? v) [cls (v props)] [cls v]))) (css cls))
          className (str (get props :className "") " " varClass)
          props (assoc props :className className)
          props (apply dissoc props attrs)]
      (apply js/React.createElement tag (clj->js props) (html children)))))
