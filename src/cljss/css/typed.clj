(ns cljss.css.typed
  "CSS Typed OM Level 1
  Spec https://drafts.css-houdini.org/css-typed-om/
  Article https://developers.google.com/web/updates/2018/03/cssom"
  (:require [clojure.spec.alpha :as s]))

(s/def ::CSSNumericBaseType
  #{"length" "angle" "time" "frequency"
    "resolution" "flex" "percent"})

(s/def ::CSSMathOperator
  #{"sum" "product" "negate" "invert" "min" "max"})

(s/def ::CSSUnit
  #{"number" "percent" "%" "em" "ex"
    "ch" "ic" "rem" "lh" "rlh" "vw" "vh"
    "vi" "vb" "vmin" "vmax" "cm" "mm" "Q"
    "in" "pt" "pc" "px" "deg" "grad" "rad"
    "turn" "s" "ms" "Hz" "kHz" "dpi" "dpcm"
    "dppx" "fr"})

(s/def ::CSSUnitValue
  (s/cat
    :value number?
    :unit ::CSSUnit))

(s/def ::CSSNumericValue
  (s/or
    :unit-value ::CSSUnitValue
    :math-value ::CSSMathValue))

(s/def ::CSSNumberish
  (s/or
    :number-value number?
    :numeric-value ::CSSNumericValue))

(s/def ::CSSMathSum
  (s/cat
    :operator #{:math/+}
    :operands (s/+ ::CSSNumberish)))

(s/def ::CSSMathProduct
  (s/cat
    :operator #{:math/*}
    :operands (s/+ ::CSSNumberish)))

(s/def ::CSSMathValue
  (s/alt
    :math-sum ::CSSMathSum
    :math-product ::CSSMathProduct))

(s/def ::CSSPositionValue
  (s/cat
    :x ::CSSNumericValue
    :y ::CSSNumericValue))

;; CSS Transform
(s/def ::CSSTranslate
  (s/cat
    :op #{:translate}
    :x ::CSSNumericValue
    :y ::CSSNumericValue
    :z (s/? ::CSSNumericValue)))

(s/def ::CSSRotate
  (s/alt
    :short (s/cat
             :op #{:rotate}
             :angle ::CSSNumericValue)
    :full (s/cat
            :op #{:rotate}
            :x ::CSSNumberish
            :y ::CSSNumberish
            :z ::CSSNumberish
            :angle ::CSSNumericValue)))

(s/def ::CSSScale
  (s/cat
    :op #{:scale}
    :x ::CSSNumberish
    :y ::CSSNumberish
    :z (s/? ::CSSNumberish)))

(s/def ::CSSTransformComponent
  (s/alt
    :translate ::CSSTranslate
    :rotate ::CSSRotate
    :scale ::CSSScale))

(s/def ::CSSTransformValue
  (s/coll-of ::CSSTransformComponent :min-count 1))

;; ========================================
(defn -dispatch-compile-typed-css [v]
  (first v))

(defmulti compile-typed-css #'-dispatch-compile-typed-css)

(defmethod compile-typed-css :unit-value
  [[_ {:keys [value unit]}]]
  `(js/CSSUnitValue. ~value ~unit))

(defmethod compile-typed-css :math-value
  [[_ math-value]]
  (compile-typed-css math-value))

(defmethod compile-typed-css :numeric-value
  [[_ value]]
  (compile-typed-css value))

(defmethod compile-typed-css :number-value)
(defmethod compile-typed-css :number-value
  [[_ value]]
  value)

(defmethod compile-typed-css :math-sum
  [[_ {:keys [operands]}]]
  `(js/CSSMathSum. ~@(map compile-typed-css operands)))

(defmethod compile-typed-css :math-product
  [[_ {:keys [operands]}]]
  `(js/CSSMathProduct. ~@(map compile-typed-css operands)))

(defmethod compile-typed-css :translate
  [[_ {:keys [x y z]}]]
  `(js/CSSTranslate. ~@(->> (filter identity [x y z])
                            (map compile-typed-css))))

(defmethod compile-typed-css :rotate
  [[_ [_ {:keys [angle]}]]]
  `(js/CSSRotate. ~(compile-typed-css angle)))

(defmethod compile-typed-css :scale
  [[_ {:keys [x y z]}]]
  `(js/CSSScale. ~@(->> (filter identity [x y z])
                        (map compile-typed-css))))

(comment
  (let [ast (s/conform ::CSSUnitValue [16 "px"])]
    (compile-typed-css [:unit-value ast])))
