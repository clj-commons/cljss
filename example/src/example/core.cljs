(ns example.core
  (:require [rum.core :as rum]
            [reagent.core :as r]
            [om.dom :as dom]
            [om.next :as om :refer [defui]]
            [goog.dom :as gdom]
            [cljss.core :refer [defstyles defstyled]]))

(defstyles wrapper [width]
  {:padding "8px 16px"
   :margin "0 auto"
   :text-align "center"
   :font "normal 18px sans-serif"
   :width width})

(defstyled H1 :h1
  {:font-size "32px"
   :color "#242424"
   :margin-top :v-margin
   :margin-bottom :v-margin})


;; Rum
(rum/defc RumApp []
  [:div {:class (wrapper "600px")}
   (H1 {:v-margin "8px"} "Clojure Style Sheets for Rum")])

(rum/mount (RumApp) (gdom/getElement "rum-app"))


;; Reagent
(defn ReagentApp []
  [:div {:class (wrapper "600px")}
   [H1 {:v-margin "8px"} "Clojure Style Sheets for Reagent"]])

(r/render [ReagentApp] (gdom/getElement "reagent-app"))


;; Om
(defui OmApp
  Object
  (render [this]
    (dom/div #js {:className (wrapper "600px")}
      (H1 {:v-margin "8px"} "Clojure Style Sheets for Om"))))

(.render js/ReactDOM ((om/factory OmApp)) (gdom/getElement "om-app"))
