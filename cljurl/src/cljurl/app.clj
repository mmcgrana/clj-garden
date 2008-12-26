(ns cljurl.app
  (:require
    [cwsg.middleware.show-exceptions :as show-exceptions]
    [cswg.middleware.file-content-info :as file-content-info]
    [cswg.middleware.string-content-length :as string-content-length]
    ring.app
    cljurl.routing
    cljurl.app.controllers
    [cljurl.config :as config])
  (:import java.io.File))

(def app
  (show-exceptions/wrap
    (file-content-info/wrap
      (string-content-length/wrap
        (static/wrap config/+public-dir+
          (ring.app/spawn-app cljurl.routing/action-recognizer))))))
