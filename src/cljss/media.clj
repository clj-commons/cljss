(ns cljss.media
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as cstr]
            [cljss.utils :refer [literal?]]
            [cljss.collect :as c]
            [cljss.utils :as utils]))

;;
;; CSS Media Queries Level 4 spec
;;

(s/def ::modifiers #{:not :only})
(s/def ::logical-operators #{:and :not :only})
(s/def ::media-types #{:all :print :screen :speech})

(s/def ::media-feature-name
  #{:aspect-ratio :color-gamut :color :color-index :any-hover :monochrome
    :grid :width :orientation :update :scripting :resolution :hover
    :inverted-colors :pointer :display-mode :any-pointer :scan
    :overflow-block :overflow-inline :light-level :height
    :max-width :min-width :max-height :min-height
    :max-aspect-ratio :min-aspect-ratio :max-resolution :min-resolution
    :max-color :min-color :max-color-index :min-color-index
    :max-monochrome :min-monochrome :device-height :device-width
    :max-device-height :max-device-width :min-device-height :min-device-width
    :device-aspect-ratio :max-device-aspect-ratio :min-device-aspect-ratio})

(s/def ::media-feature-value some?)

(s/def ::media-feature-plain
  (s/cat
    :feature-name ::media-feature-name
    :feature-value ::media-feature-value))

(s/def ::media-feature-boolean ::media-feature-name)

(s/def ::media-feature-range-basic-operand
  (s/alt
    :feature-name ::media-feature-name
    :feature-value ::media-feature-value))

(s/def ::media-feature-range-basic
  (s/cat
    :left ::media-feature-range-basic-operand
    :operator #{'= '< '<= '> '>=}
    :right ::media-feature-range-basic-operand))

(s/def ::media-feature-range-complex-left-dir
  (s/cat
    :left ::media-feature-value
    :left-operator #{'< '<=}
    :feature-name ::media-feature-name
    :right-operator #{'< '<=}
    :right ::media-feature-value))

(s/def ::media-feature-range-complex-right-dir
  (s/cat
    :left ::media-feature-value
    :left-operator #{'> '>=}
    :feature-name ::media-feature-name
    :right-operator #{'> '>=}
    :right ::media-feature-value))

(s/def ::media-feature-range-complex
  (s/alt
    :left-dir ::media-feature-range-complex-left-dir
    :right-dir ::media-feature-range-complex-right-dir))

(s/def ::media-feature-range
  (s/alt
    :basic-range ::media-feature-range-basic
    :complex-range ::media-feature-range-complex))

(s/def ::media-feature
  (s/alt
    :plain ::media-feature-plain
    :boolean ::media-feature-boolean
    :range ::media-feature-range))

(s/def ::media-condition
  (s/alt
    :modifier (s/? ::media-not)
    :condition (s/cat
                 :in-parens ::media-in-parens
                 :conditions (s/* (s/alt
                                    :modifier ::media-and
                                    :modifier ::media-or)))))

(s/def ::media-in-parens
  (s/or
    :condition ::media-condition
    :feature ::media-feature))

(s/def ::media-not
  (s/cat
    :logical #{:not}
    :in-parens ::media-in-parens))

(s/def ::media-and
  (s/cat
    :logical #{:and}
    :in-parens ::media-in-parens))

(s/def ::media-or
  (s/cat
    :logical #{:or}
    :in-parens ::media-in-parens))

(s/def ::media-condition-without-or
  (s/alt
    :condition ::media-not
    :condition (s/cat
                 :in-parens ::media-in-parens
                 :conditions (s/* ::media-and))))

(s/def ::media-query
  (s/alt
    :condition ::media-condition
    :query (s/cat
             :modifier (s/? ::modifiers)
             :media-type ::media-types
             :condition (s/?
                          (s/cat
                            :logical #{:and}
                            :cond-without-or ::media-condition-without-or)))))

;;
;; AST compiler
;;

(defn compile-media-query-dispatch [ast]
  (cond
    (= :query (first ast)) :query
    (= :modifier (first ast)) :modifier
    (= :media-type (first ast)) :media-type
    (= :condition (first ast)) :condition
    (= :conditions (first ast)) :conditions
    (= :logical (first ast)) :logical
    (= :cond-without-or (first ast)) :cond-without-or
    (= :in-parens (first ast)) :in-parens
    (= :feature (first ast)) :feature
    (= :feature-name (first ast)) :feature-name
    (= :feature-value (first ast)) :feature-value
    (= :plain (first ast)) :plain
    (= :range (first ast)) :range
    (= :basic-range (first ast)) :basic-range
    (= :complex-range (first ast)) :complex-range
    (= :boolean (first ast)) :boolean
    :else (first ast)))

(defmulti compile-media-query #'compile-media-query-dispatch)

(defmethod compile-media-query :query [[_ query]]
  (->> (seq query) (map compile-media-query)))

(defmethod compile-media-query :modifier [[_ modifier]]
  (name modifier))

(defmethod compile-media-query :media-type [[_ media-type]]
  (name media-type))

(defmethod compile-media-query :condition [[_ condition]]
  (->> (seq condition) (map compile-media-query)))

(defmethod compile-media-query :conditions [[_ conditions]]
  (mapcat
    #(compile-media-query [:condition %])
    conditions))

(defmethod compile-media-query :logical [[_ logical]]
  (name logical))

(defmethod compile-media-query :cond-without-or [[_ condition]]
  (compile-media-query condition))

(defmethod compile-media-query :in-parens [[_ in-parens]]
  (compile-media-query in-parens))

(defmethod compile-media-query :feature [[_ feature]]
  (compile-media-query feature))

(defmethod compile-media-query :feature-name [[_ feature-name]]
  (name feature-name))

(defmethod compile-media-query :feature-value [[_ feature-value]]
  (cond
    (keyword? feature-value) (name feature-value)
    (symbol? feature-value) (name feature-value)
    :else feature-value))

(defmethod compile-media-query :plain [[_ {:keys [feature-name feature-value]}]]
  (str "("
       (compile-media-query [:feature-name feature-name]) ":"
       (compile-media-query [:feature-value feature-value]) ")"))

(defmethod compile-media-query :range [[_ range]]
  (compile-media-query range))

(defmethod compile-media-query :boolean [[_ bool]]
  (str "(" (name bool) ")"))

(defmethod compile-media-query :basic-range [[_ {:keys [left operator right]}]]
  (str
    "("
    (compile-media-query left) " "
    (name operator) " "
    (compile-media-query right)
    ")"))

(defmethod compile-media-query :complex-range
  [[_ [_ {:keys [left left-operator feature-name right-operator right]}]]]
  (str
    "("
    (compile-media-query [:feature-value left]) " "
    (name left-operator) " "
    (compile-media-query [:feature-name feature-name]) " "
    (name right-operator) " "
    (compile-media-query [:feature-value right])
    ")"))

(->> [:only :screen :and [:orientation :portrait]
      :or :not :all :and [:orientation :portrait]
      :or :not :print :and [:orientation :portrait]
      :or [:color]
      :or [:orientation :portrait] :and [:orientation :portrait]]
     (s/explain ::media-query))







(s/def ::media-directives #{:max-width :min-width})

(s/def ::directive
  (s/and
    (s/conformer seq)
    (s/coll-of
      (s/cat
        :directive ::media-directives
        :value some?))))

(s/def ::css (s/map-of keyword? some?))

(s/def ::directives-block
  (s/and
    (s/conformer seq)
    (s/coll-of
      (s/cat
        :directives ::directive
        :styles ::css))))

(defn parse-media [styles]
  (s/conform ::directives-block styles))

(defn explain-media [styles]
  (s/explain ::directives-block styles))

(defn compile-media-dispatch [styles]
  (cond
    (contains? styles :media) :media
    (contains? styles :directives) :directives
    (contains? styles :directive) :directive))

(defmulti compile-media #'compile-media-dispatch)

(defmethod compile-media :media [{media :media}]
  (->> media
       (reduce
         (fn [[styles svalues] directives]
           (let [[static values] (compile-media directives)]
             [`(cljs.core/str ~styles "@media " ~static) (concat svalues values)]))
         ["" []])))


(defmethod compile-media :directives [{:keys [directives styles]}]
  (let [pseudo     (filterv utils/pseudo? styles)
        pstyles    (->> pseudo
                        (reduce
                          (fn [coll [rule styles]]
                            (conj coll (c/collect-styles (str (:cls @c/env*) (subs (name rule) 1)) styles)))
                          []))
        styles     (filterv (comp not utils/pseudo?) styles)
        [static values] (c/collect-styles (:cls @c/env*) styles)
        values     (->> pstyles
                        (mapcat second)
                        (into values))
        directives (map compile-media directives)]
    [`(cljs.core/str
        (cstr/join " and " ~directives)
        ~(str "{" (apply str static (map first pstyles)) "}"))
     values]))

(defmethod compile-media :directive [{:keys [directive value]}]
  (if (literal? value)
    (str "(" (name directive) ":" value ")")
    `(cljs.core/str "(" ~(name directive) ":" ~value ")")))

(defn build-media [styles]
  (let [result (parse-media styles)]
    (if (= result ::s/invalid)
      (throw (Error. (explain-media styles)))
      (compile-media {:media result}))))
