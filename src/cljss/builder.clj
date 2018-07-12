(ns cljss.builder
  (:require [cljss.media :refer [build-media]]
            [cljss.collect :as c]
            [cljss.utils :as utils]))

(defn status? [[rule value]]
  (and (re-matches #"^.*\?$" (name rule))
       (map? value)))

(defn build-styles [cls styles]
  (c/reset-env! {:cls cls})
  (let [pseudo  (filterv utils/pseudo? styles)
        [mstatic mvals] (some-> styles :cljss.core/media build-media)
        styles  (dissoc styles :cljss.core/media)
        styles  (filterv (comp not utils/pseudo?) styles)
        [static vals] (c/collect-styles cls styles)
        pstyles (->> pseudo
                     (reduce
                       (fn [coll [rule styles]]
                         (conj coll (c/collect-styles (str cls (subs (name rule) 1)) styles)))
                       []))
        vals    (->> pstyles
                     (mapcat second)
                     (into vals)
                     (concat mvals)
                     (into []))
        static (into [static] (map first pstyles))
        static (if mstatic
                 (conj static mstatic)
                 static)]
    [cls static vals]))

(comment
  (build-styles
    "hello"
    {:background    'color
     :width         "100px"
     :height        "100px"
     :border-radius "5px"
     :padding       "8px"
     :cljss.core/media    {[[:max-width "740px"]]
                           {:width  "64px"
                            :height "64px"}}}))
