(ns cljss.css.props.display
  (:require [clojure.spec.alpha :as s]))

(s/def ::display
  #{:inline-block :block :inherit :table-row-group :table-row :table
    :inline-table :table-column-group :list-item :table-header-group
    :table-column :table-cell :table-footer-group :table-caption :none
    :inline})

(defmulti compile-css identity)

(defmethod compile-css :default [value]
  (name value))
