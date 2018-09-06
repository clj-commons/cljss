(ns cljss.utils
  (:require [clojure.string :as cstr]))

#?(:cljs (def dev? ^boolean goog.DEBUG))

#?(:clj
   (defn cljs-env?
     "Take the &env from a macro, and tell whether we are expanding into cljs."
     [env]
     (boolean (:ns env))))

(defn pseudo? [[rule value]]
  (and (re-matches #"&:.*" (name rule))
       (map? value)))

(defn nested? [[rule value]]
  (and (string? rule)
       (map? value)))

(defn literal? [x]
  (or (string? x) (number? x)))

(defn escape-val [rule val]
  (if (= rule :content)
    (pr-str val)
    val))

(defn build-css [cls styles]
  (->> styles
       (map (fn [[rule val]] (str (name rule) ":" (escape-val rule val) ";")))
       (cstr/join "")
       (#(str "." cls "{" % "}"))))

(defn compile-css-rule [[rule val]]
  (let [r [(str (name rule) ":")]
        r (if (vector? val)
            (into r val)
            (conj r val))]
    (conj r ";")))

(defn reduce-str [s]
  (->> s
       (reduce
         (fn [s s1]
           (let [s0    (first s)
                 srest (rest s)]
             (if (and (literal? s1) (string? s0))
               (cons (str s0 s1) srest)
               (cons s1 s))))
         (list ""))
       reverse))

#?(:clj
   (defn resolve-get
     "Tries to resolve a var and get its value. Returns the symbol if failed."
     [sym]
     (try
       (var-get (resolve sym))
       (catch Exception e
         sym))))

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

(defn with-meta? [v]
  #?(:cljs (satisfies? IWithMeta v)
     :clj  (instance? clojure.lang.IObj v)))

(defn -mk-var-class [props vars cls static]
  (let [vars (->> vars
                  (map (fn [[cls v]]
                         (cond
                           (and (ifn? v) (with-meta? v))
                           (->> v meta list flatten (map #(get props % nil)) (apply v) (list cls))

                           (ifn? v)
                           (list cls (v props))

                           :else (list cls v)))))]
    #?(:cljs (cljss.core/css cls static vars)
       :clj  [cls static vars])))

(defn -meta-attrs [vars]
  (->> vars
       (map second)
       (filter with-meta?)
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
