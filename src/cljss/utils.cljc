(ns cljss.utils
  (:require [clojure.string :as cstr]))

#?(:cljs (def dev? ^boolean goog.DEBUG))

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
           (let [s0 (first s)
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
