(ns cljss.core
  (:require [cljss.utils :refer [build-css]]))

(defn- varid [id idx [rule]]
  [rule (str "--css-" id "-" idx)])

(defn- dynamic? [[_ value]]
  (not (or (string? value)
           (number? value))))

(defn- pseudo? [[rule value]]
  (and (re-matches #"&:.*" (name rule))
       (map? value)))

(defn- build-styles [cls id idx styles]
  (let [dynamic (filterv dynamic? styles)
        static (filterv (comp not dynamic?) styles)
        vars (map-indexed #(varid id (+ idx %1) %2) dynamic)
        vals (mapv (fn [[_ var] [_ exp]] [var exp]) vars dynamic)
        static (->> vars
                    (map (fn [[rule var]] [rule (str "var(" var ")")]))
                    (concat static)
                    (build-css cls))]
    [static vals (count vars)]))

(defmacro defstyled [var tag styles]
  (let [tag# (name tag)
        pseudo (filterv pseudo? styles)
        styles (filterv (comp not pseudo?) styles)
        id# (-> styles hash str)
        cls (str "css-" id#)
        attrs# (->> styles (map second) (filterv keyword?))
        [static vals idx] (build-styles cls id# 0 styles)
        pstyles (->> pseudo
                     (map (fn [[rule styles]]
                            (build-styles (str cls (subs (name rule) 1)) id# idx styles))))
        static# (->> pstyles
                     (map first)
                     (apply str)
                     (str static))
        vals# (->> pstyles
                   (mapcat second)
                   (into vals))]
    `(def ~var
       (cljss.core/styled ~tag# ~id# ~static# ~vals# ~attrs#))))
