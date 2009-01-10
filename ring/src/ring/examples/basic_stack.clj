(ns ring.examples.basic-stack
  (:require (ring.middleware show-exceptions file-content-info static)
            ring.endpoints.dump
            ring.handlers.jetty)
  (:import (java.io File)))

(def app
  (ring.middleware.show-exceptions/wrap
    (ring.middleware.file-content-info/wrap
      (ring.middleware.static/wrap (File. "src/ring/examples/public")
        ring.endpoints.dump/app))))

(ring.handlers.jetty/run {:port 8080} app)
