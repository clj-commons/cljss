(ns example.core
  (:require [rum.core :as rum]
            [reagent.core :as r]
            [om.dom :as dom]
            [om.next :as om :refer [defui]]
            [goog.dom :as gdom]
            [cljss.core :refer [defstyles defkeyframes font-face inject-global]]
            [cljss.reagent :as rss :include-macros true]
            [cljss.rum :as rumss :include-macros true]
            [cljss.om :as omss :include-macros true]))

(def font-size 16)

(inject-global
  {:body {:margin 0
          :font (str "normal " font-size "px sans-serif")
          :color "#242424"}
   "#app label" {:color "red"}})

(def font-name "Example Font")
(def font-url "examplefont")

(font-face {:font-family   font-name
            :src           [{:url    (str font-url ".woff")
                             :format "woff"}]})

(defkeyframes bounce [bounce-height]
  {[:from 20 53 80 :to] {:transform "translate3d(0,0,0)"}
   [40 43]              {:transform (str "translate3d(0,-" bounce-height "px,0)")}
   70                   {:transform (str "translate3d(0,-" (/ bounce-height 2) "px,0)")}
   90                   {:transform (str "translate3d(0,-" (/ bounce-height 4) "px,0)")}})

(defstyles wrapper [v-padding]
  {:padding-top v-padding
   :padding-bottom v-padding
   :text-align "center"
   :font "normal 18px Example Font, sans-serif"})

(def color (r/atom "#856dcf"))

;;; Rum
(rumss/defstyled RumH1 :h1
  {:font-size "48px"
   :color :color
   :margin-top :v-margin
   :margin-bottom :v-margin
   :active? {:font-size "14px"}})

(rum/defcs RumTitle <
  rum/reactive
  (rum/local 30 ::state)
  [{state ::state}]
  [:div {:class (wrapper "8px")
         :style {:animation (str (bounce @state) " 1s ease infinite")}
         :css {:border-bottom (str "1px solid " @color)}
         :on-click #(swap! state (partial * 2))}
   (RumH1 {:v-margin "8px"
           :color (rum/react color)
           :active? false}
          "Clojure Style Sheets for Rum")])

;; Reagent
(rss/defstyled ReagentH1 :h1
  {:font-size "48px"
   :color :color
   :margin-top :v-margin
   :margin-bottom :v-margin})

(defn ReagentTitle []
  [:div {:class (wrapper "8px")}
   [ReagentH1 {:v-margin "8px" :color @color}
    "Clojure Style Sheets for Reagent"]])


;; Om
(omss/defstyled OmH1 :h1
  {:font-size "48px"
   :color :color
   :margin-top :v-margin
   :margin-bottom :v-margin})

(defui OmTitle
  Object
  (render [this]
    (dom/div #js {:className (wrapper "8px")}
      (OmH1 {:v-margin "8px"
             :color @color}
            "Clojure Style Sheets for Om"))))



(rss/defstyled InputField :div
  {:display "flex"
   :flex-direction "column"
   :justify-content "center"
   :align-items "center"})

(rss/defstyled InputLabel :label
  {:font "normal 14px sans-serif"})

(rss/defstyled Input :input
  {:border-radius "2px"
   :border "1px solid #ccc"
   :padding (with-meta #(str %1 " " %2) [:padding-v :padding-h])})

(defn App []
  [InputField
   [InputLabel "text color"]
   [Input {:onChange #(reset! color (.. % -target -value))
           :padding-v "4px"
           :padding-h "8px"}]])

(defn mount []
  (rum/mount (RumTitle) (gdom/getElement "rum-app"))
  (r/render [ReagentTitle] (gdom/getElement "reagent-app"))
  (om/add-root! (om/reconciler {:state color}) OmTitle (gdom/getElement "om-app"))
  (r/render [App] (gdom/getElement "app")))

(mount)
