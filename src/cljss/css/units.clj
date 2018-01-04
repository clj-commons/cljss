(ns cljss.css.units
  (:require [clojure.spec.alpha :as s]))

;;
;; Numeric data types
;;

(s/def ::integer int?)

(s/def ::number number?)

(s/def ::percentage
  (s/cat
    :value (s/and
             number?
             #(s/int-in-range? 0 101 %))
    :unit #{:%}))

(s/def ::angle
  (s/cat
    :value
    (s/and
      number?
      #(s/int-in-range? 0 361 %))))

(s/def ::length-percentage
  (s/alt
    :length ::length
    :percentage ::percentage))

;;
;; CSS Units
;;

(s/def ::length-units
  #{:em :ex :ch :rem :vw :vh :vmin :vmax
    :cm :mm :q :in :pc :pt :px})

(s/def ::angle-units
  #{:deg :grad :rad :turn})

(s/def ::duration-units
  #{:s :ms})

(s/def ::frequency-units
  #{:hz :khz})

(s/def ::resolution-units
  #{:dpi :dpcm :dppx})

;;
;; Dimensions (numbers with units)
;;

(s/def ::length
  (s/cat
    :value ::number
    :unit ::length-units))

(s/def ::time
  (s/cat
    :value ::number
    :unit ::duration-units))

(s/def ::frequency
  (s/cat
    :value ::number
    :unit ::frequency-units))

(s/def ::resolution
  (s/cat
    :value ::number
    :unit ::resolution-units))

;;
;; Generic CSS Dimensions spec
;;


(s/def ::dimension
  (s/alt
    :length ::length
    :time ::time
    :frequency ::frequency
    :resolution ::resolution))


(comment
  (s/conform ::dimension [100 :ms]))



(defmulti compile-css first)

(defmethod compile-css :length [[_ {:keys [value unit]}]]
  (str value (name unit)))

(defmethod compile-css :time [[_ {:keys [value unit]}]]
  (str value (name unit)))

(defmethod compile-css :frequency [[_ {:keys [value unit]}]]
  (str value (name unit)))

(defmethod compile-css :resolution [[_ {:keys [value unit]}]]
  (str value (name unit)))

(defmethod compile-css :numeric [[_ {:keys [value]}]]
  (str value))

(defmethod compile-css :percentage [[_ {:keys [value]}]]
  (str value "%"))

(defmethod compile-css :keyword [[_ value]]
  (name value))
