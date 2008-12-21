(ns cljurl.app
  (:use ring.app)
  (:require ring.app
            cljurl.routing
            cljurl.app.controllers))

(def app (spawn-app cljurl.routing/action-recognizer))
