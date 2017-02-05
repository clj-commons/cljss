(ns cljss.core
  (:require [cljs.analyzer.api :as ana-api]
            [clojure.string :as s]))

(def css-output-to (:css-output-to (ana-api/get-options)))

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

(defmacro defstyles
  "Creates a mapping from style names to generated unique names.

  (defstyles styles ::list
    {:list {:list-style \"none\"}
     :list-item {:height \"48px\"}})

  styles  ;; => {:list \"list31247\", :list-item \"list-item31248\"}

  Arguments:
    id     - fully-qualified keyword
    styles - hash map of styles definitions"
  [name id styles]
  (let [names (atom {})
        parsed (map (fn [[selector rules]]
                      [(->unique-name selector names)
                       (->css-rules rules)])
                    styles)
        css (->> parsed
              (map (fn [[selector rules]] (str "." selector rules)))
              (reduce str))
        styles->css (->> (interleave (keys styles) (map first parsed))
                      (partition 2)
                      (map #(into [] %))
                      (into {}))]
    (if css-output-to
      (do
        (spit css-output-to css :append true)
        `(def ~name ~styles->css))
      `(def ~name (let [tag# (or (js/document.head.querySelector (str "style[data-id=\"" ~id "\"]"))
                                 (js/document.createElement "style"))]
                    (set! (.. tag# -dataset -id) ~id)
                    (set! (.. tag# -innerText) ~css)
                    (js/document.head.appendChild tag#)
                    ~styles->css)))))
