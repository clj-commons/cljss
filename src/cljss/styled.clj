(ns cljss.styled
  (:require [clojure.spec.alpha :as s]))

(defn -defstyled-type-dispatch [[_ marker]]
  (if (= '< marker)
    :with-mixins
    :no-mixins))

(defmulti defstyled-type #'-defstyled-type-dispatch)

(defmethod defstyled-type :no-mixins [_]
  (s/cat
    :var-name symbol?
    :tag-name keyword?
    :styles map?))

(defmethod defstyled-type :with-mixins [_]
  (s/cat
    :var-name symbol?
    :mixins-marker #{'<}
    :mixins (s/+ symbol?)
    :tag-name keyword?
    :styles map?))

(def defstyled
  (s/multi-spec defstyled-type :defstyled-type))

(defn parse-defstyled [args]
  (s/conform defstyled args))

(comment
  (parse-defstyled '[Button < mixins/spacing :button {:padding "0 8px"}]))

(comment
  '{:var-name      Button,
    :mixins-marker <,
    :mixins        [mixins/spacing],
    :tag-name      :button,
    :styles        {:padding "0 8px"}})
