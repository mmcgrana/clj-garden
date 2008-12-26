(ns cljurl.app
  (:require
    cwsg.middleware.show-exceptions
    ring.app
    cljurl.routing
    cljurl.app.controllers))

(def app
  (cwsg.middleware.show-exceptions/wrap
    (ring.app/spawn-app cljurl.routing/action-recognizer)))
