(ns cljss.core-test
  (:require [cljs.test :refer-macros [is testing deftest]])
  (:require-macros [cljss.test-macros :refer [test-styled]]))

(deftest test->styled
  (testing "static css only"
    (let [[tag static vals attrs] (test-styled Test :h1 {:color "white"})]
      (is (= "h1" tag))
      (is (= ".cljss_core-test__Test{color:white;}" static))))

  (testing "static with attrs"
    (let [[tag static vals attrs] (test-styled Test :h1 {:v-margin   "8px"
                                                         :margin-top :v-margin})
          val                     (aget vals 0)
          varname                 (aget val 0)
          class-name              ".cljss_core-test__Test"
          attr                    (aget attrs 0)]
      (is (= static (str class-name "{v-margin:8px;margin-top:var(" varname ");}")))
      (is (= :v-margin attr))))

  (testing "dynamic values"
    (let [[tag static vals attrs] (test-styled Test :h1 {:margin #(if (:large %) "10px" "5px")})
          val                     (aget vals 0)
          varname                 (aget val 0)]
      (is (= "--var-cljss_core-test__Test-0" varname))
      (is (= (str ".cljss_core-test__Test{margin:var(" varname ");}") static))))

  (testing "pseudo styles"
    (let [[tag static vals attrs]
          (test-styled Test :h1 {:color "white" :&:hover {:color "red"}})
          class-name ".cljss_core-test__Test"]
      (is (= static (str class-name "{color:white;}" class-name ":hover{color:red;}")))))

  (testing "dynamic pseudo styles"
    (let [[tag static vals attrs]
          (test-styled
            Test
            :h1
            {:color "blue" :&:hover {:color #(if (:bright %) "white" "black")}})
          val        (aget vals 0)
          varname    (aget val 0)
          class-name ".cljss_core-test__Test"]
      (is (= "--var-cljss_core-test__Test:hover-0" varname))
      (is (= static (str class-name "{color:blue;}" class-name ":hover{color:var(" varname ");}")))))

  (testing "status attributes"
    (let [[tag static vals attrs]
          (test-styled Test :h1 {:font-size "48px" :active? {:font-size "14px"}})
          class-name ".cljss_core-test__Test"
          val        (aget vals 0)
          varname    (aget val 0)
          func       (aget val 1)]
      (is (= static (str class-name "{font-size:var(" varname ");}")))
      (is (= "14px" (func {:active? true})))))

  (testing "a complete set of styles"
    (let [[tag static vals attrs]
          (test-styled
            Test
            :h1
            {:color            "white"
             :font-size        "48px"
             :background-color #(if (= "dark" (:theme %)) "black" "white")
             :&:hover          {:color "green"
                                :text-decoration
                                #(if (= "underlined" (:decoration %)) "underline" "wavy")}
             :active?          {:font-size "14px"}})
          class-name ".cljss_core-test__Test"
          val-0      (aget vals 0)
          varname-0  (aget val-0 0)
          val-1      (aget vals 1)
          varname-1  (aget val-1 0)
          val-2      (aget vals 2)
          varname-2  (aget val-2 0)]
      (is (= static (-> class-name
                      (str "{color:white;")
                      (str "font-size:var(" varname-0 ");")
                      (str "background-color:var(" varname-1 ");}")
                      (str class-name ":hover")
                      (str "{color:green;")
                      (str "text-decoration:var(" varname-2 ");}")))))))
