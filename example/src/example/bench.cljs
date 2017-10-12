(ns example.bench
  (:refer-clojure :exclude [println])
  (:require [rum.core :as rum]
            [goog.dom :as gdom]
            [cljss.core :refer [defstyles]]))

(def state* (atom {:lines []}))

(def println print)

(set! *print-fn* #(swap! state* update :lines conj %))

(rum/defc Console < rum/reactive []
  [:ul {}
   (for [line (:lines (rum/react state*))]
     [:li {}
      line])])

(defstyles dot [t n]
  {:width "16px"
   :height "16px"
   :background "red"
   :position "absolute"
   :top (str (+ 200 (* 200 (js/Math.cos (/ (* t n) 5000)))) "px")
   :left (str (+ 200 (* 200 (js/Math.sin (/ (* t n) 5000)))) "px")})

(rum/defcs Animation <
  {:after-render
   (fn [{t ::t :as st}]
     (js/requestAnimationFrame #(reset! t (js/performance.now)))
     st)}
  (rum/local 0 ::t)
  [{t ::t}]
  [:div {:css {:position "relative"
               :width "100vw"
               :height "460px"}}
   (for [n (range 1 20)]
     [:div {:key n
            :class (dot @t n)}])])


(rum/defc App []
  [:div {:css {:font "normal 16px sans-serif"}}
   (Console)
   (Animation)])

(defstyles cell [idx row]
  {:width            "32px"
   :height           "32px"
   :top              (str idx "px")
   :background-color (str "rgb(0, " row ", 0)")
   :color            "#fff"
   :font             "normal 16px sans-serif"
   :border           "1px solid #fff"
   :border-radius    "4px"
   :display          "flex"
   :justify-content  "center"
   :align-items      "center"})

(println ";; rules insertion")
(simple-benchmark [id (atom 1)] (cell @id (int (* 255 (js/Math.abs (js/Math.sin (swap! id inc)))))) 10000)

(rum/mount (App) (gdom/getElement "app"))
