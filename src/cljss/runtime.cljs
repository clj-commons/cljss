(ns cljss.runtime
  (:require [clojure.string :as cstr]))

(def cache (atom {}))

(declare interpret-styles-map)

(defn interpret-styles-entry [[k v]]
  (str (name k) ":" v ";"))

(defn interpret-pseudo [cls [pselector styles]]
  (let [cls (cstr/replace (name pselector) "&:" (str cls ":"))
        styles-str (reduce #(str %1 (interpret-styles-entry %2)) "" styles)
        styles-block (str "." cls "{" styles-str "}")]
    styles-block))

(defn interpret-styles-map [cls styles]
  (let [{pseudo true styles false} (group-by #(-> % first name (cstr/starts-with? "&:")) styles)
        pseudo (reduce #(str %1 (interpret-pseudo cls %2)) "" pseudo)
        styles-str (reduce #(str %1 (interpret-styles-entry %2)) "" styles)
        styles-block (str "." cls "{" styles-str "}" pseudo)]
    styles-block))

(defn build-styles
  ([styles]
   (build-styles nil styles))
  ([cls styles]
   (let [id (or cls (str "css-" (hash styles)))]
     (if-some [css (get cache id)]
       [id css]
       (let [css (interpret-styles-map id styles)]
         (swap! cache assoc id css)
         [id css])))))

(comment
  (let [[cls static] (build-styles {:margin 0})]
    (cljss.core/css cls [static] [])))
