(ns ringblog.app
  (:use ring.app)
  (:require
    (cwsg.middleware
      [show-exceptions   :as show-exceptions]
      [file-content-info :as file-content-info]
      [static            :as static]
      [reloading         :as reloading])
    (ringblog
      [config :as config]
      routing controllers)))

(def app
  (wrap-if config/exception-details? show-exceptions/wrap
    (file-content-info/wrap
      (static/wrap config/public-dir
        (wrap-if config/reloading? (reloading/wrap config/reloadables)
          (spawn-app routing/router))))))

