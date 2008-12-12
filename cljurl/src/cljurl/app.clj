(ns cljurl.app
  (:require ring.app
            cljurl.routing
            cljurl.app.controllers))

(def app (ring.app/spawn-app cljurl.routing/action-recognizer))
