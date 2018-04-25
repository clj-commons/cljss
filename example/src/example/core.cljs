(ns example.core
  (:require [rum.core :as rum]
            [cljss.core :as css :refer [inject-global]]
            [cljss.rum :refer-macros [defstyled]]
            [devcards.core :as dc :refer [defcard]]
            [sablono.core :refer [html]]))

;; utils
(defn space-between [space items]
  (html
    [:div {}
     (interpose (html [:span {:css {:margin-left space}}]) items)]))

;; design system
(def colors
  {:blue "#298FCA"
   :green "#7BC86C"
   :orange "#FFB968"
   :red "#EF7564"
   :yellow "#F5DD29"})

(rum/defc Text
  [{:keys [size]}
   child]
  [:div
   {:css {:font-family "Helvetica Neue"
          :font-size size}}
   child])

(defn P [opts child]
  (let [opts (assoc opts :size "14px")]
    (Text opts child)))

(defn H1 [opts child]
  (let [opts (assoc opts :size "48px")]
    (Text opts child)))

(defn H2 [opts child]
  (let [opts (assoc opts :size "40px")]
    (Text opts child)))

(def button->color
  {:warning (:orange colors)
   :error (:red colors)
   :ok (:green colors)})

(rum/defc Button
  [{:keys [kind
           on-click]}
   child]
  [:button
   {:on-click on-click
    :css {:background (get button->color kind)
          :border 0
          :border-radius "5px"
          :padding "8px 24px"
          :font-size "14px"
          :color "#fff"}}
   child])

;; cards
(defcard Colors
  "Base colors"
  (fn [state _]
    (html
      [:div {:css {:display "flex"
                   :justify-content "space-between"}}
       (for [[_ color] (:colors @state)]
         [:div
          {:css {:background color
                 :width "100px"
                 :height "100px"
                 :border-radius "5px"
                 :padding "8px"}}
          color])]))
  {:colors colors})

(defcard Typography
  (html
    [:div {}
     (H1 {} "Heading One")
     (H2 {} "Heading Two")
     (P {} "Paragraph Text")]))

(defcard Buttons
  (space-between
    "8px"
    [(Button {:kind :warning} "Warning")
     (Button {:kind :error} "Error")
     (Button {:kind :ok} "OK")]))


(defn mount []
  (css/remove-styles!))

(mount)
