(ns ring.examples.hello_world
  (:require ring.handlers.jetty))

(defn app
  [env]
  {:status  400
   :headers {"Content-Type" "text/html"}
   :body    "Hello World from Ring"})

(ring.handlers.jetty/run {:port 8080} app)
