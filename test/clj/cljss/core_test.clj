(ns cljss.core-test
  (:require [clojure.test :refer :all]
            [cljss.core :refer :all]
            [cljss.font-face :as ff]
            [cljss.inject-global :as ig]))

(def basic-styles {:color "red"})

(def dynamic-styles {:background-color #(:bg %)
                     :margin           #(if (:large %) "10px" "5px")})

(def pseudo-styles (merge basic-styles {:&:hover {:color "blue"}}))

(def pseudo-dynamic-styles
  (assoc
    pseudo-styles
    :&:active
    {:color #(:active-color %)}))

(def complete-styles
  (merge
    basic-styles
    dynamic-styles
    pseudo-dynamic-styles))

;;; unique ids for style classes are generated
;;; from the hashed set of non pseudo styles
;;; this function replicates initialization
;;; of styles in cljss.core/build-styles
;;; and produces the same hashed value used in
;;; style ids
(defn remove-pseudo [styles]
  (filterv
    (comp not #'cljss.core/pseudo?)
    styles))

(deftest test-build-styles
  (testing "building basic styles"
    (let [[static vals] (build-styles "test" basic-styles)]
      (is (= (str ".test{color:red;}") static))
      (is (empty? vals))))

  (testing "building dynamic styles"
    (let [[static vals] (build-styles "test" dynamic-styles)]
      (is (= (str ".test{background-color:var(--var-test-0);margin:var(--var-test-1);}") static))
      (is (= [["--var-test-0" (:background-color dynamic-styles)] ["--var-test-1" (:margin dynamic-styles)]] vals))))

  (testing "building basic pseudo styles"
    (let [[static vals] (build-styles "test" pseudo-styles)]
      (is (= (str ".test{color:red;}.test:hover{color:blue;}") static))
      (is (empty? vals))))

  (testing "building dynamic psuedo styles"
    (let [[static vals] (build-styles "test" pseudo-dynamic-styles)]
      (is (= (-> ".test"
                 (str "{color:red;}")
                 (str ".test:hover{color:blue;}")
                 (str ".test:active{color:var(--var-test:active-0);}")) static))
      (is (= [["--var-test:active-0" (get-in pseudo-dynamic-styles [:&:active :color])]] vals))))

  (testing "building a complete set of styles"
    (let [[static vals] (build-styles "test" complete-styles)]
      (is (= (-> ".test"
                 (str "{color:red;")
                 (str "background-color:var(--var-test-0);")
                 (str "margin:var(--var-test-1);}")
                 (str ".test:hover{color:blue;}")
                 (str ".test:active{color:var(--var-test:active-2);}")) static))
      (is (= [["--var-test-0" (:background-color complete-styles)]
              ["--var-test-1" (:margin complete-styles)]
              ["--var-test:active-2" (get-in complete-styles [:&:active :color])]] vals)))))

(deftest test-font-face
  (testing "build @font-face"
    (is (= '(cljs.core/str
              "@font-face{"
              "font-family:\""
              font-name
              "\";font-variant:normal;font-stretch:unset;font-weight:400;font-style:normal;unicode-range:U+0025-00FF, U+0025-00FF;src:local(\"Arial\")"
              \,
              \space
              "url(\""
              (str "examplefont" ".woff")
              "\")"
              \space
              "format(\"woff\");"
              "}")
           (ff/font-face {:font-family   'font-name
                          :font-variant  "normal"
                          :font-stretch  "unset"
                          :font-weight   400
                          :font-style    "normal"
                          :unicode-range ["U+0025-00FF" "U+0025-00FF"]
                          :src           [{:local "Arial"}
                                          {:url    '(str "examplefont" ".woff")
                                           :format "woff"}]})))))

(deftest test-inject-global
  (testing "build global styles"
    (is (=
          '(["body" "body{margin:0;}"]
            ["ul" "ul{list-style:none;color:red;}"]
            ["body > .app" (cljs.core/str "body > .app" "{" "border:" (str "1px solid" color) ";" "}")])
          (ig/inject-global {:body         {:margin 0}
                             :ul           {:list-style "none"
                                            :color      "red"}
                             "body > .app" {:border '(str "1px solid" color)}})))))
