(ns user
  (:require [figwheel-sidecar.repl-api :as f]))

(defn start! [id]
  (f/start-figwheel! id)
  (f/cljs-repl))

(defn stop! []
  (f/stop-figwheel!))
