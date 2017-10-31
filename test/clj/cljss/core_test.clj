(ns cljss.core-test
  (:require [clojure.test :refer :all]
            [cljss.core :refer :all]
            [cljss.builder :refer :all]
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
;;; of styles in cljss.builder/build-styles
;;; and produces the same hashed value used in
;;; style ids
(defn remove-pseudo [styles]
  (filterv
    (comp not #'cljss.builder/pseudo?)
    styles))

(deftest test-build-styles
  (testing "building basic styles"
    (let [[[id static vals]] (build-styles "test" basic-styles)]
      (is (= "test" id))
      (is (= ".test{color:red;}" static))
      (is (empty? vals))))

  (testing "building dynamic styles"
    (let [[[id static vals]] (build-styles "test" dynamic-styles)]
      (is (= "test" id))
      (is (= ".test{background-color:var(--var-test-0);margin:var(--var-test-1);}" static))
      (is (= [["--var-test-0" (:background-color dynamic-styles)] ["--var-test-1" (:margin dynamic-styles)]] vals))))

  (testing "building basic pseudo styles"
    (let [result (build-styles "test" pseudo-styles)]
      (is (= result
             [["test" ".test{color:red;}" []]
              [".test:hover" ".test:hover{color:blue;}" []]]))))

  (testing "building dynamic pseudo styles"
    (let [result (build-styles "test" pseudo-dynamic-styles)]
      (is (= result
             [["test"
               ".test{color:red;}"
               [["--var-test-1" (get-in pseudo-dynamic-styles [:&:active :color])]]]
              [".test:hover" ".test:hover{color:blue;}" []]
              [".test:active" ".test:active{color:var(--var-test-1);}" []]]))))

  (testing "building a complete set of styles"
    (let [result (build-styles "test" complete-styles)]
      (is (= result
             [["test"
               ".test{color:red;background-color:var(--var-test-0);margin:var(--var-test-1);}"
               [["--var-test-0" (:background-color complete-styles)]
                ["--var-test-1" (:margin complete-styles)]
                ["--var-test-3" (get-in complete-styles [:&:active :color])]]]
              [".test:hover" ".test:hover{color:blue;}" []]
              [".test:active" ".test:active{color:var(--var-test-3);}" []]])))))

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
