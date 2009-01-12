; The a very simple Ring application.

(ns ring.examples.hello_world
  (:require ring.jetty))

(defn app
  [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "<h3>Hello World from Ring</h3>"})

(ring.jetty/run {:port 8080} app)
