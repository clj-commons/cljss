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
                     (concat mvals))]
    [cls
     `(cljs.core/str ~(apply str static (map first pstyles)) ~mstatic)
     vals]))

(build-styles
  "hello"
  {:cljss.core/media {{:max-width 'sa :min-width 'l} {:font-size 'a
                                                      :&:hover   {:margin 'b}}}})
