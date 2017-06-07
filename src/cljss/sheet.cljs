(ns cljss.sheet
  (:require [goog.object :as gobj]
            [goog.dom :as dom]))

(defn- make-style-tag []
  (let [tag (dom/createElement "style")
        head (aget (dom/getElementsByTagNameAndClass "head") 0)]
    (gobj/set tag "type" "text/css")
    (dom/appendChild tag (dom/createTextNode ""))
    (dom/appendChild head tag)
    tag))

(defn- find-sheet [tag]
  (if-let [sheet (gobj/get tag "sheet")]
    sheet
    ;; workaround for Firefox
    (let [sheets (gobj/get js/document "styleSheets")]
      (loop [idx 0
             sheet (aget sheets idx)]
        (if (= tag (gobj/get sheet "ownerNode"))
          sheet
          (recur (inc idx) (aget sheets (inc idx))))))))


(defprotocol ISheet
  (insert! [this css])
  (flush! [this]))

(deftype Sheet [tag]
  ISheet
  (insert! [this css]
    (let [sheet (find-sheet tag)
          rules-count (gobj/get (gobj/get sheet "cssRules") "length")]
      (if (not= (.indexOf css "@import") -1)
        (.insertRule sheet css 0)
        (.insertRule sheet css rules-count))))
  (flush! [this]
    (-> tag
        .parentNode
        (.removeChild tag))))

(defn create-sheet []
  (Sheet. (make-style-tag)))
