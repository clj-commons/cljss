(ns cljss.core
  (:require [cljss.sheet :refer [create-sheet insert! filled? flush!]]
            [cljss.utils :refer [build-css dev?]]
            [clojure.string :as cstr]))

(def ^:private sheets (atom (list (create-sheet))))

(defn remove-styles! []
  (run! flush! @sheets)
  (reset! sheets (list (create-sheet))))

(defn css
  "Takes class name, static styles and dynamic styles.
   Injects styles and returns a string of generated class names."
  [cls static vars]
  (let [static (if (string? static) [static] static)
        sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css cls static vars))
      (do
        (loop [[s & static] static
               idx 0]
          (let [cls (str cls "-" idx)]
            (insert! sheet s cls)
            (when-not (empty? static)
              (recur static (inc idx)))))
        (if (pos? (count vars))
          (let [var-cls (str "vars-" (hash vars))]
            (insert! sheet #(build-css var-cls vars) var-cls)
            (str cls " " var-cls))
          cls)))))

(defn css-keyframes
  "Takes CSS animation name, static styles and dynamic styles.
   Injects styles and returns generated CSS animation name."
  [cls static vars]
  (let [sheet (first @sheets)]
    (if (filled? sheet)
      (do
        (swap! sheets conj (create-sheet))
        (css-keyframes cls static vars))
      (let [inner
            (reduce
              (fn [s [id val]] (cstr/replace s id val))
              static
              vars)
            anim-name (str "animation-" cls "-" (hash vars))
            keyframes (str "@keyframes " anim-name "{" inner "}")]
        (insert! sheet keyframes anim-name)
        anim-name))))

;; ==============================
(defn -camel-case [k]
  (if (or (keyword? k)
          (string? k)
          (symbol? k))
    (let [[first-word & words] (cstr/split (name k) #"-")]
      (if (or (empty? words)
              (= "aria" first-word)
              (= "data" first-word))
        k
        (-> (map cstr/capitalize words)
            (conj first-word)
            cstr/join
            keyword)))
    k))

(defn -compile-class-name [props]
  (let [className
        (-> props
            (select-keys [:className :class :class-name])
            vals
            (->> (filter identity)))]
    (when-not (empty? className)
      (str (clojure.string/join " " className) " "))))

(defn -mk-var-class [props vars cls static]
  (->> vars
       (map (fn [[cls v]]
              (cond
                (and (ifn? v) (satisfies? IWithMeta v))
                (->> v meta list flatten (map #(get props % nil)) (apply v) (list cls))

                (ifn? v)
                (list cls (v props))

                :else (list cls v))))
       (css cls static)))

(defn -meta-attrs [vars]
  (->> vars
       (map second)
       (filter #(satisfies? IWithMeta %))
       (map meta)
       flatten
       set))

(defn -camel-case-attrs [props]
  (reduce-kv
    (fn [m k v]
      (let [k (case k
                :for :htmlFor
                (-camel-case k))]
        (assoc m k v)))
    {}
    props))

(defn -styled [cls static vars attrs create-element]
  (let [clsn (str cls "-" (gensym))
        static (if ^boolean dev?
                 (map #(cstr/replace % cls clsn) static)
                 static)
        vars (if ^boolean dev?
               (->> vars (map (fn [[k v]] [(cstr/replace k cls clsn) v])))
               vars)
        cls (if ^boolean dev? clsn cls)]
    (fn [props & children]
      (let [[props children] (if (map? props)
                               (array props children)
                               (array {} (apply array props children)))
            var-class (-mk-var-class props vars cls static)
            meta-attrs (-meta-attrs vars)
            className (str (-compile-class-name props) var-class)
            props (-> (apply dissoc props (concat attrs meta-attrs [:class :class-name :className]))
                      (assoc :className className)
                      -camel-case-attrs)]
        (create-element props children)))))
