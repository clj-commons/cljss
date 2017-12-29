(ns cljss.media
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as cstr]
            [cljss.utils :refer [literal?]]
            [cljss.collect :as c]
            [cljss.utils :as utils]))

;;
;; CSS Media Queries Level 4 spec
;;

(s/def ::modifiers #{:not :only})
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
  (first ast))

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
    :else feature-value))

(defmethod compile-media-query :plain [[_ {:keys [feature-name feature-value]}]]
  (let [feature-name  (compile-media-query [:feature-name feature-name])
        feature-value (compile-media-query [:feature-value feature-value])]
    (if (literal? feature-value)
      (str "(" feature-name ":" feature-value ")")
      `(cljs.core/str ~(str "(" feature-name ":") ~feature-value ")"))))

(defmethod compile-media-query :range [[_ range]]
  (compile-media-query range))

(defmethod compile-media-query :boolean [[_ bool]]
  (str "(" (name bool) ")"))

(defmethod compile-media-query :basic-range [[_ {:keys [left operator right]}]]
  (let [left     (compile-media-query left)
        right    (compile-media-query right)
        operator (name operator)]
    (if (->> [left right] (map (comp not literal?) (filter identity) seq))
      `(cljs.core/str "(" ~left " " ~operator " " ~right ")")
      (str "(" left " " operator " " right ")"))))

(defmethod compile-media-query :complex-range
  [[_ [_ {:keys [left left-operator feature-name right-operator right]}]]]
  (let [left           (compile-media-query [:feature-value left])
        feature-name   (compile-media-query [:feature-name feature-name])
        right          (compile-media-query [:feature-value right])
        left-operator  (name left-operator)
        right-operator (name right-operator)]
    (if (->> [left right] (map (comp not literal?) (filter identity) seq))
      `(cljs.core/str "(" ~left ~(str " " left-operator " " feature-name " " right-operator " ") ~right ")")
      (str "(" left " " left-operator " " feature-name " " right-operator " " right ")"))))

(defn -compile-media-query [query]
  (let [ret (s/conform ::media-query query)
        ret (if (= ::s/invalid ret)
              (throw (Error. (s/explain ::media-query query)))
              (compile-media-query ret))]
    `(cljs.core/str "@media " (clojure.string/join " " ~ret))))


(defn compile-media-dispatch [styles]
  (cond
    (contains? styles :media) :media
    (contains? styles :styles) :styles))

(defmulti compile-media #'compile-media-dispatch)

(defmethod compile-media :media [{media :media}]
  (->> (seq media)
       (reduce
         (fn [[sstyles svalues] [query styles]]
           (let [[static values] (compile-media {:styles styles})
                 query (-compile-media-query query)]
             [`(cljs.core/str ~sstyles ~query ~static) (concat svalues values)]))
         ["" []])))

(defmethod compile-media :styles [{styles :styles}]
  (let [pseudo  (filterv utils/pseudo? styles)
        pstyles (->> pseudo
                     (reduce
                       (fn [coll [rule styles]]
                         (conj coll (c/collect-styles (str (:cls @c/env*) (subs (name rule) 1)) styles)))
                       []))
        styles  (filterv (comp not utils/pseudo?) styles)
        [static values] (c/collect-styles (:cls @c/env*) styles)
        values  (->> pstyles
                     (mapcat second)
                     (into values))]
    [(str "{" (apply str static (map first pstyles)) "}")
     values]))

(defn build-media [styles]
  (compile-media {:media styles}))

(c/reset-env! {:cls "class"})

(build-media
  {[:screen :and [:min-width 'a]] {:font-size 'p
                                   :&:hover   {:color 'g}}})
