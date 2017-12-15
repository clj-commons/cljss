(ns cljss.validation.css
  (:require [clojure.spec.alpha :as s]
            [cuerdas.core :as c]
            [clj-fuzzy.metrics :as fuzzy]
            [cheshire.core :as json]
            [cljss.validation.css-properties :as css-props]))

;;
;; HEX Color
;;

(s/def ::hex-char #{\a \b \c \d \e \f \A \B \C \D \E \F \0 \1 \2 \3 \4 \5 \6 \7 \8 \9})

(defn hex-spec [n]
  (s/and
    string?
    (s/conformer #(vector (first %) (rest %)))
    (s/cat
      :marker #(= % \#)
      :color (s/and #(= (count %) n) (s/+ ::hex-char)))
    (s/conformer #(:color %))))

(s/def ::hex-rgb (hex-spec 3))
(s/def ::hex-rgba (hex-spec 4))
(s/def ::hex-rrggbb (hex-spec 6))
(s/def ::hex-rrggbbaa (hex-spec 8))


;;
;; RGB(A) Color
;;

(s/def ::rgb-range #(s/int-in-range? 0 256 %))
(s/def ::alpha-range (s/and #(>= % 0) #(<= % 1)))

(s/def ::rgb
  (s/cat
    :red ::rgb-range
    :green ::rgb-range
    :blue ::rgb-range))

(s/def ::rgba
  (s/cat
    :red ::rgb-range
    :green ::rgb-range
    :blue ::rgb-range
    :alpha ::alpha-range))

(s/def ::color
  (s/alt
    :hex-rgb ::hex-rgb
    :hex-rgba ::hex-rgba
    :hex-rrggbb ::hex-rrggbb
    :hex-rrggbbaa ::hex-rrggbbaa
    :rgba ::rgba
    :rgb ::rgb))

;;
;; Margin
;;

(def units #{:px :em :rem :vh :vw})

(s/def ::units units)

(s/def ::numeric-value
  (s/cat
    :value number?
    :unit ::units))

(defn margin-dispatch [value]
  (count value))

(defmulti margin #'margin-dispatch)

(defmethod margin 2 [_]
  ::numeric-value)

(defmethod margin 4 [_]
  (s/and
    (s/conformer #(partition 2 %))
    (s/cat
      :vertical (s/spec ::numeric-value)
      :horizontal (s/spec ::numeric-value))))

(defmethod margin 6 [_]
  (s/and
    (s/conformer #(partition 2 %))
    (s/cat
      :top (s/spec ::numeric-value)
      :horizontal (s/spec ::numeric-value)
      :bottom (s/spec ::numeric-value))))

(defmethod margin 8 [_]
  (s/and
    (s/conformer #(partition 2 %))
    (s/cat
      :top (s/spec ::numeric-value)
      :right (s/spec ::numeric-value)
      :bottom (s/spec ::numeric-value)
      :left (s/spec ::numeric-value))))

(s/def ::margin (s/multi-spec margin :margin))

;;
;; Validation
;;

;; Rules validation

(def valid-keys css-props/props)

(defn validate-keys [valid-keys value]
  (let [invalid-keys (->> (keys value)
                          (filter (comp not valid-keys)))

        rated
        (for [valid-key valid-keys
              invalid-key invalid-keys]
          [invalid-key
           valid-key
           (fuzzy/dice (name invalid-key) (name valid-key))])

        misspelled
        (->> rated
             (group-by first)
             (map (fn [[n variants]]
                    [n
                     (->> variants
                          (sort-by last >)
                          (take 4)
                          (map second))])))]
    misspelled))

(defn validate-css-properties [styles]
  (let [warnings
        (->> (validate-keys valid-keys styles)
             (map (fn [[misspelled [best-match & suggestions]]]
                    (c/istr "Looks like you've misspelled \"~{misspelled}\" CSS property. Should it be \"~{best-match}\" or one of these: ~{suggestions}?"))))]
    (doseq [w warnings]
      (println
        (c/<<-
          (c/istr "WARNING - Misspelled CSS property
        ~{w}"))))))

;; Values validation

(def rule-validators
  {:background-color ::color
   :margin ::margin})



(defn explain-problem-dispatch [_ _ problem]
  (-> problem :via last))

(defmulti explain-problem #'explain-problem-dispatch)

(defmethod explain-problem ::rgb-range
  [rule-name value {:keys [path pred val via in]}]
  (let [[color-type] path
        color-type (name color-type)]
    (c/istr "Invalid color channel value ~{val} found in \"~{rule-name} ~{value}\". Make sure the value is within range 0-255.")))

(defmethod explain-problem ::alpha-range
  [rule-name value {:keys [path pred val via in]}]
  (let [[color-type] path
        color-type (name color-type)
        val (c/quote (str val))]
    (c/istr "Invalid alpha channel value ~{val} found in \"~{rule-name} ~{value}\". Make sure the value is within range 0-1.")))

(defmethod explain-problem ::color
  [rule-name value {:keys [path pred val via in]}]
  (let [color-value (c/join ", " value)
        val (c/quote (str val))]
    (c/istr "Invalid color value ~{val}. Perhaps wrong length?")))

(defmethod explain-problem ::numeric-value
  [rule-name value {:keys [path pred val via in]}]
  (let [value-type (type val)
        val (c/quote (str val))]
    (c/istr "Invalid value ~{val} of type \"~{value-type}\" found in \"~{rule-name} ~{value}\". Number is expected.")))

(defmethod explain-problem ::units
  [rule-name value {:keys [path pred val via in]}]
  (let [val-str (name val)
        [best-match & suggestions]
        (->> units
             (map name)
             (map #(fuzzy/levenshtein val-str %))
             (interleave units)
             (partition 2)
             (sort-by last <)
             (map first))]
    (c/istr "Invalid unit ~{val} found in \"~{rule-name} ~{value}\". Did you mean ~{best-match} or one of these ~{suggestions}?")))



(defn explain-problems [value-spec rule-name target-value]
  (let [{::s/keys [problems value]} (s/explain-data value-spec target-value)]
    (->> problems
         (map #(explain-problem rule-name value %))
         first)))

(defn valid-rule? [rule-name value]
  (if-not (s/valid? (rule-validators rule-name) value)
    [rule-name {:error (explain-problems (rule-validators rule-name) rule-name value)}]
    [rule-name {:ast (s/conform (rule-validators rule-name) value)}]))

(defn validate-styles [styles]
  (->> styles
       (filter (fn [[rule]] (contains? rule-validators rule)))
       (map #(apply valid-rule? %))))


(comment
  (validate-styles {:margin [8 :p]}))


;; compile conform produced AST into CSS string
(defn compile-ast-dispatch [value-type _]
  value-type)

(defmulti compile-ast #'compile-ast-dispatch)

(defmethod compile-ast :rgb [_ {:keys [red green blue]}]
  (c/istr "rgb(~{red},~{green},~{blue})"))

(defmethod compile-ast :rgba [_ {:keys [red green blue alpha]}]
  (c/istr "rgba(~{red},~{green},~{blue},~{alpha})"))

(defn compile-styles [[rule {:keys [ast]}]]
  [rule (apply compile-ast ast)])


;;
;; Usage
;;

(comment
  (s/conform ::color "#ff0")
  (s/conform ::color "#ff00")
  (s/conform ::color "#ff00ff")
  (s/conform ::color "#ff00ff00")
  (s/conform ::color [123 0 98])
  (s/conform ::color [123 0 98 0.4])

  (s/explain-data ::color [123 0 255 1.2]))
