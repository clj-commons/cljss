(ns cljss.utils
  (:require [clojure.string :as cstr]))

(defn escape-val [rule val]
  (if (= rule :content)
    (pr-str val)
    val))

(defn build-css [cls styles]
  (->> styles
       (map (fn [[rule val]] (str (name rule) ":" (escape-val rule val) ";")))
       (cstr/join "")
       (#(str "." cls "{" % "}"))))

(defn empty-css? [css]
  (->> css (re-matches #"^.*\{\}?") boolean))
