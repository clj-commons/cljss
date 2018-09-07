(ns example.ssr
  (:require [rum.core :as rum]
            [cljss.core :as css]
            [cljss.rum :refer [defstyled]]
            [cljss.ssr :as ssr]
            [sablono.core :refer [html]]
            [clojure.string :as cstr]))

(def colors
  {:blue   "#298FCA"
   :green  "#7BC86C"
   :orange "#FFB968"
   :red    "#EF7564"
   :yellow "#F5DD29"})

(def button->color
  {:warning (:orange colors)
   :error   (:red colors)
   :ok      (:green colors)})

(rum/defc Button
  [{:keys [kind
           on-click]}
   child]
  [:button
   {:on-click on-click
    :css      {:background    (get button->color kind)
               :border        "none"
               :border-radius "5px"
               :padding       "8px 24px"
               :font-size     "14px"
               :color         "#fff"
               :&:hover       {:color  "black"
                               :cursor "pointer"}
               :&:focus       {:outline "none"}}}
   child])

(css/defstyles Color [color]
  {:background    color
   :width         "100px"
   :height        "100px"
   :border-radius "5px"
   :padding       "8px"
   :&:hover       {:border "1px solid blue"}
   ::css/media    {[[:max-width "740px"]]
                   {:width  "64px"
                    :height "64px"}}})

(rum/defcs Colors <
  (rum/local {:colors colors} :state)
  [{state :state}]
  [:div {:css {:display         "flex"
               :justify-content "space-between"}}

   (for [[_ color] (:colors @state)]
     [:div {:class (Color color)}
      color])])

(css/defstyles text-styles [size]
  {:font-family "Helvetica Neue"
   :font-size   size})

(rum/defc Text
  [{:keys [size]}
   child]
  [:div
   {:class (text-styles size)}
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

(rum/defc Typography []
  [:div {}
   (H1 {} "Heading One")
   (H2 {} "Heading Two")
   (P {} "Paragraph Text")])

(rum/defc space-between [space items]
  [:div {}
   (interpose [:span {:css {:margin-left space}}] items)])

(rum/defc Buttons []
  (space-between
    "8px"
    [(Button {:kind :warning} "Warning")
     (Button {:kind :error} "Error")
     (Button {:kind :ok} "OK")]))

(def spinner-sizes
  {"m"  "16px"
   "l"  "32px"
   "xl" "64px"})

(css/defkeyframes spin []
  {:from {:transform "rotate(0deg)"}
   :to   {:transform "rotate(360deg)"}})

(rum/defc spinner
  [{:keys [size color]}]
  [:div {:css {:width         (spinner-sizes size)
               :height        (spinner-sizes size)
               :border-radius "50%"
               :border-top    (str "2px solid " color)
               :border-left   "2px solid rgba(0,0,255,0.3)"
               :border-bottom "2px solid rgba(0,0,255,0.3)"
               :border-right  "2px solid rgba(0,0,255,0.3)"
               :animation     (str (spin) " 1500ms linear infinite")}}])

(rum/defc Spinner []
  [:div {}
   (spinner {:size "m" :color "blue"})
   (spinner {:size "l" :color "blue"})
   (spinner {:size "xl" :color "blue"})])

(rum/defc Selectors []
  [:ul {}
   [:li {:css {"&:first-child" {:color "red"}}} "first-child"]
   [:li {:css {".link" {:color "green"}}}
    [:a.link {:href "#"} "nested element with .link class name"]]])

(defstyled text-field :textarea
  {:width         :width
   :height        :height
   :border-radius "5px"
   :border        "1px solid #ccc"
   :&:hover       {:background-color "#eee"}
   :&:focus       {:outline "none"
                   :border  "1px solid blue"}})

(rum/defc TextField []
  (text-field {:width  "300px"
               :height "140px"}))

(rum/defc app []
  [:div {}
   (Colors)
   (Typography)
   (Buttons)
   (Spinner)
   (Selectors)
   (TextField)])

;;
;; =============================

#?(:cljs
   (rum/hydrate (app) (.querySelector js/document "#root")))

#?(:clj
   (defn render []
     (binding [ssr/*ssr-ctx* (atom {:styles {}})]
       (let [html     (app)
             [html css] (ssr/render-css html)
             css-tag  (str "<style>" css "</style>")        ;; inline critical CSS
             css-link (str "<link rel=\"stylesheet\" href=\"/ssr.css\">") ;; serve all static styles
             html     (rum/render-html html)
             html     (str "<div id=\"root\">" html "</div>")
             html     (str css-link
                           css-tag
                           html
                           "<script src=\"/js/compiled/ssr.js\"></script>")]
         (spit "example/resources/public/ssr.html" html)))))

(comment
  (render))

(comment
  (require '[cljs.build.api :as b])
  (binding [css/*exclude-static?* true
            ssr/*ssr-ctx*         (atom {})]
    (b/build
      ["src" "example/src" "example/resources"]
      {:output-to       "example/resources/public/js/compiled/ssr.js"
       :output-dir      "example/resources/public/js/compiled/ssr-min"
       :main            'example.ssr
       :optimizations   :whitespace
       :closure-defines {"goog.DEBUG" false}})
    (->> (:static @ssr/*ssr-ctx*)
         ssr/ctx->css-str
         (spit "example/resources/public/ssr.css"))))
