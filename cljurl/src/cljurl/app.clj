(ns cljurl.app
  (:require
    [cwsg.middleware.show-exceptions       :as show-exceptions]
    [cwsg.middleware.file-content-info     :as file-content-info]
    [cwsg.middleware.static                :as static]
    (ring app)
    (cljurl config routing controllers)))

(def app
  (show-exceptions/wrap #(cljurl.config/show-exceptions?)
    (file-content-info/wrap
      (static/wrap cljurl.config/+public-dir+
        (ring.app/spawn-app cljurl.routing/router)))))
