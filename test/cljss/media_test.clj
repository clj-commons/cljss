(ns cljss.media-test
  (:require [clojure.test :refer :all]
            [cljss.media :refer :all]))

(deftest test-media-queries
  (testing "simple media query"
    (is (render-media-expr {:print true})
        "print"))
  (testing "simple media query with not"
    (is (render-media-expr {:print false})
        "not print"))
  (testing "comma separated media query"
    (is (render-media-expr [{:print true} {:screen false}])
        "print, not screen"))
  (testing "comma separated media query"
    (is (render-media-expr {:max-width "768px"})
        "(max-width: 768px)"))
  (testing "compound media query"
    (is (render-media-expr {:speech true :aspect-ratio "11/5"})
        "speech and (aspect-ratio: 11/5)"))
  (testing "media feature without value"
    (is (render-media-expr {:color nil})
        "(color)"))
  (testing "media feature with :only"
    (is (render-media-expr {:screen :only})
        "only screen")))
