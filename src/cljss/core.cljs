(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert!]]
            [cljss.utils :refer [build-css]]))

(defonce ^:private sheet (create-sheet))

(defn css
  "Takes class name, static styles and dynamic styles.
   Injects styles and returns a string of generated class names."
  [cls static vars]
  (when-not (empty? static)
    (insert! sheet static))
  (if (pos? (count vars))
    (let [var-cls (str "vars-" (hash vars))]
      (insert! sheet (build-css var-cls vars))
      (str "css-" cls " " var-cls))
    (str "css-" cls)))

(defn styled [tag cls static vars attrs]
  (fn [props & children]
    (let [[props children] (if (map? props) #js [props children] #js [{} (.concat #js [props] (to-array children))])
          var-class (->> vars (map (fn [[cls v]] (if (ifn? v) #js [cls (v props)] #js [cls v]))) (css cls static))
          className (:className props)
          className (str (when className (str className " ")) var-class)
          props (assoc props :className className)
          props (apply dissoc props attrs)]
      (apply js/React.createElement tag (clj->js props) children))))
