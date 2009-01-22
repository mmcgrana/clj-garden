(ns cling.app
  (:use
    (ring builder))
  (:require
    [cling.config :as config]
    (ring backtrace file-info static reload)
    (weld app)
    (cling controllers)))

(def app
  (wrap-if config/backtracing? ring.backtrace/wrap
    (ring.file-info/wrap
      (ring.static/wrap config/public config/statics
        (wrap-if config/reloading? (partial ring.reload/wrap config/reloadables)
          weld.app/app)))))
