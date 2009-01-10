(ns ring.middleware.reloading)

(defn wrap
  "Wrap an app such that before a request is passed to the app, each namespace
  identified by syms in reloadables is reloaded."
  ([reloadables app]
   (fn [env]
     (doseq [ns-sym reloadables]
       (require ns-sym :reload))
     (app env)))
  ([reloadables]
   (partial wrap reloadables)))