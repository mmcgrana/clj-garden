(ns weldblog.app
  (:use
    ring.builder weldblog.config)
  (:require
    (ring backtrace file-info static reload)
    weld.app
    weldblog.controllers))

(def app
  (wrap-if backtracing? ring.backtrace/wrap
    (ring.file-info/wrap
      (ring.static/wrap public statics
        (wrap-if reloading? (partial ring.reload/wrap reloadables)
          weld.app/app)))))
