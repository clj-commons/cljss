(ns cljss.core
  (:require [cljs.analyzer :as ana]
            [cljs.analyzer.api :as ana-api]
            [clojure.string :as s]))

(def css-output-to
  (when cljs.env/*compiler*
    (:css-output-to (ana-api/get-options))))

(when css-output-to
  (spit css-output-to ""))

(defn- escape-val [r-name r-val]
  (cond
    (= r-name "content") (pr-str r-val)
    :else r-val))

(defn- parse-selector [selector]
  (s/split (name selector) #"((?<=:)|(?=:))"))

(defn- ->unique-name [selector names]
  (let [[class & pseudo] (parse-selector (name selector))
        uclass (or (get @names class) (gensym class))]
    (swap! names assoc class uclass)
    (->> uclass
         (conj pseudo)
         (apply str))))

(defn- ->css-rules [rules]
  (->> rules
    (map (fn [[r-name r-val]]
           (let [r-name (name r-name)]
             [r-name (escape-val r-name r-val)])))
    (map (fn [[r-name r-val]] (str r-name ":" r-val ";")))
    (reduce str "{")
    (#(str % "}"))))

(defn- parse-styles [names styles]
  (map (fn [[selector rules]]
        [(->unique-name selector names)
         (->css-rules rules)])
      styles))

(defn- ->css-str [parsed]
  (->> parsed
       (map (fn [[selector rules]] (str "." selector rules)))
       (reduce str)))

(defn- styles->names [styles parsed]
  (->> (interleave (keys styles) (map first parsed))
       (partition 2)
       (filter #(->> % first name (re-matches #".*:.*") not))
       (map #(into [] %))
       (into {})))

(defmacro defstyles
  "Creates a mapping from style names to generated unique names.

  (defstyles styles
    {:list {:list-style \"none\"}
     :list-item {:height \"48px\"}})

  styles  ;; => {:list \"list31247\", :list-item \"list-item31248\"}"
  [var styles]
  (let [parsed (parse-styles (atom {}) styles)
        css (->css-str parsed)
        styles->css (styles->names styles parsed)]
    (if css-output-to
      (do
        (spit css-output-to css :append true)
        `(def ~var ~styles->css))
      (let [id (->> (ana/resolve-var &env var) :name str)]
        `(def ~var (let [tag# (or (js/document.head.querySelector (str "style[data-id=\"" ~id "\"]"))
                                  (js/document.createElement "style"))]
                     (set! (.. tag# -dataset -id) ~id)
                     (set! (.. tag# -innerText) ~css)
                     (js/document.head.appendChild tag#)
                     ~styles->css))))))
