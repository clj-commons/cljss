(ns cljss.atomic
  (:require [cljss.utils :refer [build-css]]))

(def styles (atom {}))

(defn- next-int []
  (inc (count @styles)))

(defn- char-range
  [a b]
  (mapv char
        (range (int a) (int b))))

(def ^:private valid-chars
  (concat (char-range \a \z)
          (char-range \A \Z)
          (char-range \0 \9)
          [\_ \-]))

(defn- unique-id-gen [ids]
  (apply concat
         (iterate #(for [x %
                         y ids]
                     (str x y))
                  (map str ids))))

(def ^:private inf-ids-seq (unique-id-gen valid-chars))

(defn- new-class []
  (nth inf-ids-seq (next-int)))

(defn add-css-prop [pv]
  (or
    (get @styles pv)
    (let [gen-class (new-class)]
      (swap! styles assoc pv gen-class)
      gen-class)))

(defmacro insert-css! []
  (let [styles (mapv (fn [[s cls]] [cls (build-css cls [s])]) @styles)]
    `(do
       ~@(mapv (fn [[cls css]] `(cljss.core/insert-css! ~css ~cls)) styles))))
