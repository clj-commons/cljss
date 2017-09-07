(ns cljss.core
  (:require [cljss.utils :refer [build-css escape-val]]
            [clojure.string :as cstr]))

(defn- varid [id idx [rule]]
  [rule (str "--css-" id "-" idx)])

(defn- dynamic? [[_ value]]
  (not (or (string? value)
           (number? value))))

(defn- pseudo? [[rule value]]
  (and (re-matches #"&:.*" (name rule))
       (map? value)))

(defn- collect-styles [cls id idx styles]
  (let [dynamic (filterv dynamic? styles)
        static (filterv (comp not dynamic?) styles)
        vars (map-indexed #(varid id (+ idx %1) %2) dynamic)
        vals (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        static (->> vars
                    (map (fn [[rule var]] [rule (str "var(" var ")")]))
                    (concat static)
                    (build-css cls))]
    [static vals (count vars)]))

(defn build-styles [styles]
  (let [pseudo (filterv pseudo? styles)
        styles (filterv (comp not pseudo?) styles)
        id (-> styles hash str)
        cls (str "css-" id)
        [static vals idx] (collect-styles cls id 0 styles)
        pstyles (->> pseudo
                     (map (fn [[rule styles]]
                            (collect-styles (str cls (subs (name rule) 1)) id idx styles))))
        static (->> pstyles
                    (map first)
                    (apply str)
                    (str static))
        vals (->> pstyles
                  (mapcat second)
                  (into vals))]
    [id static vals]))

(defmacro defstyles
  "Takes var name, a vector of arguments and a hash map of styles definition.
   Generates class name, static and dynamic parts of styles.
   Returns a function that calls `cljss.core/css` to inject styles at runtime
   and returns generated class name."
  [var args styles]
  (let [[id# static# vals#] (build-styles styles)]
    `(defn ~var ~args
       (cljss.core/css ~id# ~static# ~vals#))))

(defn- vals->array [vals]
  (let [arrseq (mapv (fn [[var val]] `(cljs.core/array ~var ~val)) vals)]
    `(cljs.core/array ~@arrseq)))

(defn ->styled
  "Takes var name, HTML tag name and a hash map of styles definition.
   Returns a var bound to the result of calling `cljss.core/styled`,
   which produces React element and injects styles."
  [var tag styles]
  (let [tag (name tag)
        [id static vals] (build-styles styles)
        vals (vals->array vals)
        attrs (->> styles (map second) (filterv keyword?))
        attrs `(cljs.core/array ~@attrs)]
    [tag id static vals attrs]))

(defmacro make-styled []
  '(defn styled [cls static vars attrs create-element]
     (fn [props & children]
       (let [[props children] (if (map? props) [props children] [{} (apply vector props children)])
             var-class (->> vars (map (fn [[cls v]] (if (ifn? v) (array cls (v props)) (array cls v)))) (cljss.core/css cls static))
             className (:className props)
             className (str (when className (str className " ")) var-class)
             props (assoc props :className className)
             props (apply dissoc props attrs)]
         (create-element props children)))))

(defn- keyframes-styles [id idx styles]
  (let [dynamic (filterv dynamic? styles)
        static (filterv (comp not dynamic?) styles)
        vars (map-indexed #(varid id (+ idx %1) %2) dynamic)
        vals (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        static (->> vars
                    (map (fn [[rule var]] [rule (str "var(" var ")")]))
                    (concat static)
                    (map (fn [[rule val]] (str (name rule) ":" (escape-val rule val) ";")))
                    (cstr/join "")
                    (#(str "{" % "}")))]
    [static vals (count vars)]))

(defn- ->ks-key [k]
  (cond
    (keyword? k) (name k)
    (number? k) (str k "%")
    (vector? k) (->> k (map ->ks-key) (cstr/join ","))
    :else k))

(defn- build-keyframes [keyframes]
  (let [id (-> keyframes hash str)
        [ks [statics vals]]
        (->> keyframes
             (reduce
               (fn [[ks [static vals idx]] [k styles]]
                 (let [[s v idx] (keyframes-styles id idx styles)]
                   [(conj ks (->ks-key k))
                    [(conj static s) (into vals v) idx]]))
               [[] [[] [] 1]]))]
    [id
     (str "animation-" id)
     (->> (interleave ks statics)
          (apply str)
          (#(str "@keyframes animation-" id " {" % "}")))
     vals]))

(defmacro defkeyframes
  "Takes var name, a vector of arguments and a hash map of CSS keyframes definition.
  Generates CSS animation name, static and dynamic parts of keyframes.
  Returns a function that calls `cljss.core/css-keyframes` to inject styles at runtime\n
  and returns generated CSS animation name that can be used in CSS `animation` rule.

  (defkeyframes spin [start end]\n    {:from {:transform (str \"rotate(\" start \"deg)\")}\n     :to   {:transform (str \"rotate(\" end \"deg)\")}})

  (defstyled Spinner :div\n    {:animation (str (spin 0 180) \" 1s ease infinite\")})"
  [var args keyframes]
  (let [[id# anim-name# keyframes# vals#] (build-keyframes keyframes)]
    `(defn ~var ~args
       (cljss.core/css-keyframes ~id# ~anim-name# ~keyframes# ~vals#))))
