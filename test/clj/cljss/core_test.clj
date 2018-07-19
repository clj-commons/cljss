(ns cljss.core-test
  (:require [clojure.test :refer :all]
            [cljss.core :refer :all]
            [cljss.rum :refer :all]
            [cljss.builder :refer :all]
            [cljss.font-face :as ff]
            [cljss.inject-global :as ig]))

(deftest test-styles-builder
  (testing "static"
    (is (= (build-styles "cls" {:color "#fff"})
           ["cls" [".cls{color:#fff;}"] []])))

  (testing "pseudos"
    (is (= (build-styles "cls" {:&:hover {:color "red"}})
           ["cls" [".cls{}" ".cls:hover{color:red;}"] []])))

  (testing "string selector"
    (is (= (build-styles "cls" {"a" {:color "blue"}})
           ["cls" [".cls{}" ".cls a{color:blue;}"] []])))

  (testing "dynamic"
    (is (= (build-styles "cls" {:color 'x})
           '["cls" [".cls{color:var(--var-cls-0);}"] [["--var-cls-0" x]]])))

  (testing "dynamic pseudos"
    (is (= (build-styles "cls" {:&:hover {:color 'x}})
           '["cls" [".cls{}" ".cls:hover{color:var(--var-cls-0);}"] [["--var-cls-0" x]]]))))

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
