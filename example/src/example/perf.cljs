(ns example.perf
  (:require-macros [cljss.core])
  (:require [cljss.core :as css :refer-macros [defstyles]]
            [rum.core :as rum]
            [goog.dom :as gdom]))

(defstyles button [font-size]
  {:color            "#fff"
   :background-color "rgb(0, 0, 255)"
   :border-radius    "5px"
   :padding          "8px 24px"
   :border           "none"
   :font-size        (str font-size "px")
   :&:hover          {:background-color "rgb(0, 0, 230)"
                      :font-size        (str (* 1.2 font-size) "px")}
   "span"            {:vertical-align "middle"}})

(println "compute defstyles")
(simple-benchmark [] (button (js/Math.random)) 1000)

(rum/defc NoStylesButton []
  [:button {} "button"])

(rum/defc InlineButton []
  (let [font-size (* 10 (js/Math.random))]
    [:button {:style {:color            "#fff"
                      :background-color "rgb(0, 0, 255)"
                      :border-radius    "5px"
                      :padding          "8px 24px"
                      :border           "none"
                      :font-size        (str font-size "px")}}
     "button"]))

(rum/defc CSSAttrButton []
  (let [font-size (* 10 (js/Math.random))]
    [:button {:css {:color            "#fff"
                    :background-color "rgb(0, 0, 255)"
                    :border-radius    "5px"
                    :padding          "8px 24px"
                    :border           "none"
                    :font-size        (str font-size "px")}}
     "button"]))

(println "render NoStylesButton")
(simple-benchmark [] (rum/mount (NoStylesButton) (gdom/getElement "NoStylesButton")) 1000)

(println "render InlineButton")
(simple-benchmark [] (rum/mount (InlineButton) (gdom/getElement "InlineButton")) 1000)

(println "render CSSAttrButton")
(simple-benchmark [] (rum/mount (CSSAttrButton) (gdom/getElement "CSSAttrButton")) 1000)
