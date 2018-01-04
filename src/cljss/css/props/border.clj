(ns cljss.css.props.border
  (:require [clojure.spec.alpha :as s]
            [cljss.css.units :as units]
            [cljss.css.colors :as colors]))

(s/def ::keyword-width
  #{:thin :medium :thick})

(s/def ::width
  (s/or
    :keyword ::keyword-width
    :length ::units/length))

(s/def ::style
  #{:none :hidden :dotted :dashed :solid
    :double :groove :ridge :inset :outset})

(s/def ::shorthand
  (s/cat
    :border-width ::width
    :border-style ::style
    :border-color ::colors/color))


(s/def ::top
  (s/cat
    :top ::shorthand))

(s/def ::right
  (s/cat
    :right ::shorthand))

(s/def ::bottom
  (s/cat
    :bottom ::shorthand))

(s/def ::left
  (s/cat
    :left ::shorthand))

(s/def ::border
  (s/and
    ::shorthand
    (s/conformer
      (fn [border]
        {:top    border
         :right  border
         :bottom border
         :left   border}))))

(comment
  (s/conform ::border [[2 :px] :solid [:hex "000"]])
  (s/conform ::top [[2 :px] :solid [:hex "000"]]))
