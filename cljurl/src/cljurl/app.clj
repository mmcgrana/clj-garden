(ns cljurl.app
  (:require
    (cwsg.middleware
      [shoe-exception :as show-exceptions]
      [file-info      :as file-info]
      [static         :as static]
      [reloading      :as reloading])
    [ring.app :as app]
    (cljurl
      [config :as config]
      routing controllers)))

(def app
  (app/wrap-if config/show-exceptions?
    show-exceptions/wrap
    (file-info/wrap
      (static/wrap config/public-dir
        (app/wrap-if config/reloading?
          (partial reloading/wrap config/reloadables)
          (app/spawn-app cljurl.routing/router))))))
