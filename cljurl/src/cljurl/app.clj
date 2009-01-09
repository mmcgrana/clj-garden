(ns cljurl.app
  (:require
    (cwsg.middleware
      [show-exceptions       :as show-exceptions]
      [file-content-info     :as file-content-info]
      [static                :as static]
      [reloading             :as reloading])
    [ring.app :as app]
    (cljurl
      [config :as config]
      routing controllers)))

(def app
  (app/wrap-if config/show-exceptions?
    show-exceptions/wrap
    (file-content-info/wrap
      (static/wrap config/public-dir
        (app/wrap-if config/reloading?
          (partial reloading/wrap config/reloadable-ns-syms)
          (app/spawn-app cljurl.routing/router))))))
