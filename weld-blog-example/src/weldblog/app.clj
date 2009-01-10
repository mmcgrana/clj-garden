(ns weldblog.app
  (:use weld.app ring.builder)
  (:require
    (ring.middleware
      [show-exceptions   :as show-exceptions]
      [file-content-info :as file-content-info]
      [static            :as static]
      [reloading         :as reloading])
    (weldblog
      [config :as config]
      [routing :as routing]
      controllers)))

(def app (spawn-app routing/router))
  ;(wrap-if config/exception-details? show-exceptions/wrap
  ;  (file-content-info/wrap
  ;    (static/wrap config/public-dir
  ;      (wrap-if config/reloading? (reloading/wrap config/reloadables)
  ;        (spawn-app routing/router))))))

