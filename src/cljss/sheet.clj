(ns cljss.sheet)

(defprotocol ISheet
  (insert! [this css])
  (rules [this]))

(declare create-sheet)

(deftype Sheet [rules]
  ISheet
  (insert! [this css]
    (if (not= (.indexOf css "@import") -1)
      (create-sheet (into [css] rules))
      (create-sheet (conj rules css))))
  (rules [this]
    rules))

(defn create-sheet
  ([] (Sheet. []))
  ([rules] (Sheet. rules)))
