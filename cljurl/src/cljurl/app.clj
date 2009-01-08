(ns cljurl.app
  (:require
    (cwsg.middleware
      [show-exceptions       :as show-exceptions]
      [file-content-info     :as file-content-info]
      [static                :as static]
      [reloading             :as reloading])
    [ring.app :as app]
    (cljurl config routing controllers)))

(def reloadable-ns-syms '(cljurl.controllers cljurl.models cljurl.views))

(defn build-app [env]
  (app/wrap-if (= env :dev)
    show-exceptions/wrap
    (file-content-info/wrap
      (static/wrap cljurl.config/+public-dir+
        (app/wrap-if (= env :dev)
          (partial reloading/wrap reloadable-ns-syms)
          (app/spawn-app cljurl.routing/router))))))
