(ns example.bench
  (:require [rum.core :as rum]
            [goog.dom :as gdom]
            [cljss.core :refer [defstyles]]))

(defstyles table []
  {:display "flex"})

(defstyles cell [row]
  {:width            "32px"
   :height           "32px"
   :background-color (str "rgb(0, " row ", 0)")
   :color            "#fff"
   :font             "normal 16px sans-serif"
   :border           "1px solid #fff"
   :border-radius    "4px"
   :display          "flex"
   :justify-content  "center"
   :align-items      "center"})

(def state (atom 1))

(rum/defc Test <
  rum/reactive
  []
  (let [idx (rum/react state)]
    [:div {:class (table)}
     (for [a (range 10)]
       [:div {:key a}
        (for [b (range 10)]
          [:div
           {:key   b
            :class (cell (int (* 255 (js/Math.abs (js/Math.sin idx)))))}
           [:span {} ^String b]])])]))

(rum/mount (Test) (gdom/getElement "app"))

(defn run-bench! []
  (js/requestAnimationFrame
    #(when (< @state 100)
       (swap! state inc)
       (run-bench!))))

(run-bench!)
