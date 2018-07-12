(defproject org.roman01la/cljss "1.6.2"
  :description "Clojure Style Sheets"

  :url "https://github.com/roman01la/cljss"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [sablono "0.8.1"]]

  :test-paths ["test/clj"]

  :profiles {:dev {:source-paths ["src" "test/clj" "example/src" "example/dev" "example/resources"]
                   :plugins      [[lein-doo "0.1.8"]
                                  [lein-cljsbuild "1.1.7"]
                                  [lein-figwheel "0.5.13"]
                                  [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]
                   :dependencies [[rum "0.11.2"]
                                  [devcards "0.2.4"]
                                  [binaryage/devtools "0.9.2"]
                                  [figwheel-sidecar "0.5.13"]
                                  [com.cemerick/piggieback "0.2.2"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :aliases {"test-all" ["do" ["test"] ["doo" "phantom" "test" "once"]]}

  :figwheel {:server-port      3450
             :http-server-root "public"}

  :cljsbuild
  {:builds [{:id           "test"
             :source-paths ["src" "test/cljs"]
             :compiler     {:output-to     "resources/public/js/testable.js"
                            :main          cljss.runner
                            :optimizations :none}}
            {:id           "example"
             :source-paths ["src" "example/src" "example/resources"]
             :figwheel     {:on-jsload example.core/mount
                            :devcards  true}
             :compiler     {:main                 example.core
                            :asset-path           "js/compiled/out-dev"
                            :output-to            "example/resources/public/js/compiled/example.js"
                            :output-dir           "example/resources/public/js/compiled/out-dev"
                            :source-map-timestamp true
                            :preloads             [devtools.preload]}}

            {:id           "example-min"
             :source-paths ["src" "example/src" "example/resources"]
             :compiler     {:output-to       "example/resources/public/js/compiled/example.js"
                            :output-dir      "example/resources/public/js/compiled/out-min"
                            :main            example.core
                            :devcards        true
                            :optimizations   :advanced
                            :closure-defines {"goog.DEBUG" false}
                            :pseudo-names    true
                            :verbose         true
                            :pretty-print    false}}]})
