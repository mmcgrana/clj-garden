(ns cljre.reloading)

(defn wrap [app]
  (fn [env]
    (require 'cljre.app :reload)
    (app env)))