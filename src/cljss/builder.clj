(ns cljss.builder
  (:require [cljss.media :refer [build-media]]
            [cljss.collect :as c]
            [cljss.utils :as utils]))

(defn status? [[rule value]]
  (and (re-matches #"^.*\?$" (name rule))
       (map? value)))

(defn build-styles [cls styles]
  (let [pseudo  (filterv utils/pseudo? styles)
        nested  (->> styles
                     (filterv (comp not utils/pseudo?))
                     (filterv utils/nested?))
        [mstatic mvals] (some-> styles :cljss.core/media ((partial build-media cls)))
        styles  (dissoc styles :cljss.core/media)
        styles  (filterv #(and (not (utils/pseudo? %)) (not (utils/nested? %))) styles)
        [static vals] (c/collect-styles cls styles)
        pstyles (->> pseudo
                     (reduce
                       (fn [coll [rule styles]]
                         (conj coll (c/collect-styles (str cls (subs (name rule) 1)) styles)))
                       []))
        nstyles (->> nested
                     (reduce
                       (fn [coll [rule styles]]
                         (conj coll (c/collect-styles (str cls " " rule) styles)))
                       []))
        vals    (->> pstyles
                     (mapcat second)
                     (into vals)
                     (concat mvals)
                     (into []))
        vals (->> nstyles
                  (mapcat second)
                  (into vals))
        static (into [static] (map first pstyles))
        static (into static (map first nstyles))
        static (if mstatic
                 (conj static mstatic)
                 static)]
    [cls static vals]))

(comment
  (build-styles
    "hello"
    {"&:first-child" {:color "red"}
     "a" {:color "blue"}}))
