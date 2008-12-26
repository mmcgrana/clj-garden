(ns cljurl.app
  (:require
    [cwsg.middleware.show-exceptions :as       show-exceptions]
    [cwsg.middleware.file-content-info :as     file-content-info]
    [cwsg.middleware.string-content-length :as string-content-length]
    [cwsg.middleware.static :as                static]
    ring.app
    ring.routing
    cljurl.routing
    cljurl.app.controllers
    [cljurl.config :as config])
  (:import java.io.File))

(def app
  (show-exceptions/wrap
    (file-content-info/wrap
      (string-content-length/wrap
        (static/wrap config/+public-dir+
          (ring.app/spawn-app cljurl.routing/router))))))
