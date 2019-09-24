(ns cljss.next
  #?(:cljs (:require [cljsjs.emotion]
                     [clojure.string :as str])))

#?(:cljs
   (do

     (defn js-val? [x]
       (not (identical? "object" (goog/typeOf x))))

     (declare convert-prop-value)

     (defn kv-conv [o k v]
       (aset o
             (if (keyword? k) (-name ^not-native k) k)
             (convert-prop-value v))
       o)

     (defn convert-prop-value [x]
       (cond
         (js-val? x) x
         (keyword? x) (-name ^not-native x)
         (map? x) (reduce-kv kv-conv #js {} x)
         (coll? x) (clj->js x)
         (ifn? x) #(apply x %&)
         :else (clj->js x)))))

(defmacro defstyles [var args & body]
  `(defn ~var ~args (.css js/emotion (convert-prop-value (do ~@body)))))

(comment
  (defmacro defstyled [var tag & body]
    (let [tag-str (if (keyword? tag) (name tag) tag)]
      `(def ~var
         (cljs.core/js-invoke (.-styled js/emotion) ~tag-str (convert-prop-value (do ~@body)))))))

(defmacro defkeyframes [var args & body]
  (if (seq args)
    `(defn ~var ~args (.keyframes js/emotion (convert-prop-value (do ~@body))))
    `(let [kf# (.keyframes js/emotion (convert-prop-value (do ~@body)))]
       (defn ~var [] kf#))))
