(ns cljurl.app
  (:require
    (cwsg.middleware
      [show-exceptions       :as show-exceptions]
      [file-content-info     :as file-content-info]
      [static                :as static]
      [reloading             :as reloading])
    (cljurl config routing controllers))
  (:use ring.app))

(def reloadable-ns-syms '(cljurl.controllers cljurl.models cljurl.views))

(defn build-app [env]
  (app/wrap-if (= env :dev)
    show-exceptions/wrap
    (file-content-info/wrap
      (static/wrap cljurl.config/+public-dir+
        (app/wrap-if (= env :dev)
          (partial reloading/wrap reloadable-ns-syms)
          (app/spawn-app cljurl.routing/router))))))
