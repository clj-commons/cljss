(ns example.core
  (:require [rum.core :as rum]
            [goog.dom :as gdom]
            [cljss.core :as css :refer [inject-global]]
            [cljss.rum :refer-macros [defstyled]]))

(defn inject-global-styles! []
  (inject-global example.styles/globals))

(def btn-bg-colors
  {:primary "#0052CC"
   :default "rgba(9, 30, 66, 0.04)"
   :warning "#FFAB00"
   :error "#DE350B"})

(def btn-bg-hover-colors
  {:primary "#0065ff"
   :default "rgba(9, 30, 66, 0.08)"
   :warning "#ffc400"
   :error "#FF5630"})

(def btn-text-colors
  {:default "#505F79"
   :primary "#fff"
   :warning "#172B4D"
   :error "#fff"})

(defstyled Button :button
  {:background-color (with-meta btn-bg-colors :kind)
   :border-radius "3px"
   :border-width 0
   :box-sizing "border-box"
   :max-width "100%"
   :color (with-meta btn-text-colors :kind)
   :padding "0 8px"
   :font-size "inherit"
   :text-align "center"
   :vertical-align "middle"
   :white-space "nowrap"
   :width "auto"
   :height "2.2857142857142856em"
   :line-height "2.2857142857142856em"
   :transition "background-color 100ms ease-out"
   :&:hover {:cursor "pointer"
             :background-color (with-meta btn-bg-hover-colors :kind)}})

(defstyled -Table :div
  {:display "table"})

(defstyled -TableRow :div
  {:display "table-row"})

(defstyled -TableCell :div
  {:display "table-cell"
   :padding "4px"})

(rum/defc Table [rows]
  (-Table {}
    (for [cells rows]
      (-TableRow {}
        (for [cell cells]
          (-TableCell {} cell))))))

(def flex-alignment
  {:vertical "column"
   :horizontal "row"})

(defstyled Header :header
  {:padding "16px"
   :display "flex"
   :flex-direction (with-meta flex-alignment :alignment)
   :align-items :align-content
   :justify-content :align-content})

(defstyled Logo :img
  {:width "155px"
   :height "68px"})

(defstyled H2 :h2
  {:font-size "18px"
   :color "#242424"
   :font-weight 400})

(defstyled H3 :h2
  {:font-size "16px"
   :color "#242424"
   :font-weight 400})

(defstyled -DemoBlock :div
  {:padding "8px"
   :border-radius "5px"
   :border "2px solid #eee"})

(rum/defc DemoBlock [{:keys [title]} child]
  [:div {:css {:margin "0 0 32px"}}
   (H3 {} "Buttons")
   (-DemoBlock {} child)])

(rum/defc ButtonsDemo []
  (DemoBlock {:title "Buttons"}
    (Table
      [[(Button {:kind :default} "Default")]
       [(Button {:kind :primary} "Primary")]
       [(Button {:kind :warning} "Warning")]
       [(Button {:kind :error} "Error")]])))

(rum/defc app []
  [:div {:css {:padding "32px"
               :max-width "640px"
               :margin "0 auto"}}
   (Header {:align-content "center" :alignment :vertical}
     (Logo {:src "https://roman01la.github.io/cljss/logo.png"})
     (H2 {} "Clojure Style Sheets"))
   (ButtonsDemo)])

(defn render []
  (rum/mount (app) (gdom/getElement "app")))

(defn mount []
  (css/remove-styles!)
  (inject-global-styles!)
  (render))

(mount)
