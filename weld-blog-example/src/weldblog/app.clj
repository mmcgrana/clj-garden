(ns weldblog.app
  (:use weld.app ring.builder)
  (:require
    (ring.middleware
      [show-exceptions :as show-exceptions]
      [file-info       :as file-info]
      [static          :as static]
      [reloading       :as reloading])
    (weldblog
      [config :as config]
      [routing :as routing]
      controllers)))

(def app
  (wrap-if config/exception-details? show-exceptions/wrap
    (file-info/wrap
      (static/wrap config/public-dir
        (wrap-if config/reloading? (reloading/wrap config/reloadables)
          (spawn-app routing/router))))))

