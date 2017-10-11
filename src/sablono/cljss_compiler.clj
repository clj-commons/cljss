(ns sablono.cljss-compiler
  (:require [sablono.compiler :as s]
            [sablono.util :as sutil]
            [cljss.core :as cljss]))

(defn- compile-class [class styles]
  (let [cls       (str "css-" (hash styles))
        gen-class `(cljss.core/css ~cls ~@(cljss/build-styles cls styles))]
    (if (seq class)
      `(apply str ~gen-class " " ~@(interpose " " class))
      gen-class)))

(defn- normalize-attr [tag name type]
  (if (and (or (= name :on-change) (= name :onChange))
           (or (= tag "textarea")
               (and (= tag "input")
                    (or (nil? type)
                        (->> type (re-matches #"^(fil|che|rad).*") nil?)))))
    :on-input
    name))

(defn- compile-prum-attrs [tag attrs]
  (->> attrs
       (map (fn [[name value]]
              [(normalize-attr tag name (:type attrs)) value]))))

(defn- compile-rum-attrs [class attrs]
  (->> attrs
       (reduce (fn [attrs [name value]]
                 (if (= name :css)
                   (assoc attrs :class (compile-class class value))
                   (assoc attrs name (s/compile-attr name value))))
               nil)))

(defn- compile-attrs
  ([attrs]
   (compile-attrs nil attrs))
  ([tag {:keys [class className class-name] :as attrs}]
   (let [class (filter identity [class className class-name])]
     (cond->> (seq attrs)
              tag (compile-prum-attrs tag)
              true (compile-rum-attrs class)
              true (sutil/html-to-dom-attrs)
              true (s/to-js)))))

;; WARNING: NEVER EVER DO THIS AT HOME!
;; swap `sablono.compiler/compile-attrs` fn with own, adjusted, implementation
(alter-var-root #'s/compile-attrs (fn [_] compile-attrs))

