(ns cljss.css.colors
  (:require [clojure.spec.alpha :as s]))

;;
;; Keyword colors
;;

(s/def ::keyword-colors
  #{:current-color :transparent
    :aquamarine :lime :deepskyblue :darksalmon :antiquewhite
    :mediumturquoise :slategrey :slategray :sienna :orange :navajowhite
    :lavenderblush :firebrick :orangered :palevioletred :lawngreen
    :seashell :lightpink :darkolivegreen :aliceblue :gray :lightsteelblue
    :whitesmoke :darkgoldenrod :tan :bisque :white :lightgreen
    :darkseagreen :crimson :darkslategray :mistyrose :chocolate :yellow
    :cadetblue :navy :ghostwhite :dimgrey :seagreen :green
    :mediumseagreen :indigo :olivedrab :cyan :peachpuff :limegreen
    :mediumslateblue :violet :sandybrown :yellowgreen :mediumspringgreen
    :steelblue :rosybrown :cornflowerblue :ivory :lightgoldenrodyellow
    :salmon :darkcyan :peru :cornsilk :lightslategray :blueviolet
    :forestgreen :lightseagreen :gold :gainsboro :darkorchid :burlywood
    :lightskyblue :chartreuse :snow :moccasin :honeydew :aqua :darkred
    :mediumorchid :lightsalmon :saddlebrown :wheat :springgreen
    :lightslategrey :darkblue :powderblue :turquoise :blanchedalmond
    :papayawhip :slateblue :lightblue :skyblue :red :lightyellow :blue
    :palegreen :greenyellow :khaki :maroon :darkgrey :midnightblue
    :floralwhite :deeppink :paleturquoise :darkkhaki :azure :indianred
    :darkviolet :mediumpurple :fuchsia :coral :mediumvioletred
    :lemonchiffon :mediumblue :darkmagenta :goldenrod :darkorange :orchid
    :plum :pink :teal :magenta :lightgrey :purple :dodgerblue
    :darkturquoise :mintcream :hotpink :thistle :royalblue :darkgreen
    :darkslateblue :silver :darkgray :grey :oldlace :mediumaquamarine
    :brown :lightgray :olive :lightcoral :tomato :lightcyan :linen
    :darkslategrey :lavender :dimgray :palegoldenrod :beige :black})

;;
;; RGB(A) colors
;;

(s/def ::rgb-range
  (s/cat
    :value
    (s/and
      number?
      #(s/int-in-range? 0 256 %))))

(s/def ::percentage
  (s/cat
    :value (s/and
             number?
             #(s/int-in-range? 0 101 %))
    :unit #{:%}))

(s/def ::numeric-percentage
  (s/alt
    :numeric ::rgb-range
    :percentage (s/spec ::percentage)))

(s/def ::alpha-channel
  (s/cat
    :numeric
    (s/cat
      :value
      (s/and
        number?
        float?
        #(>= % 0)
        #(<= % 1)))))

(s/def ::rgba
  (s/cat
    :tag #{:rgb}
    :red ::numeric-percentage
    :green ::numeric-percentage
    :blue ::numeric-percentage
    :alpha (s/? ::alpha-channel)))

;;
;; HSL(A) colors
;;

(s/def ::angle
  (s/cat
    :value
    (s/and
      number?
      #(s/int-in-range? 0 361 %))))

(s/def ::angle-percentage
  (s/alt
    :numeric ::angle
    :percentage (s/spec ::percentage)))

(s/def ::hsla
  (s/cat
    :tag #{:hsl}
    :hue ::angle-percentage
    :saturation ::angle-percentage
    :lightness ::angle-percentage
    :alpha (s/? ::alpha-channel)))

;;
;; HEX colors
;;

(s/def ::hex-chars
  #{\a \b \c \d \e \f \0 \1 \2 \3 \4 \5 \6 \7 \8 \9})

(s/def ::hex
  (s/cat
    :tag #{:hex}
    :value
    (s/and
      (s/conformer seq)
      (s/+ ::hex-chars)
      (s/conformer #(apply str %)))))

;;
;; Generic CSS Color spec
;;

(s/def ::color
  (s/or
    :keyword ::keyword-colors
    :rgba ::rgba
    :hsla ::hsla
    :hex ::hex))

(comment
  (s/conform ::color [:hex "242424"]))
