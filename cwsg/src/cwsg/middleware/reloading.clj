(ns cwsg.middleware.reloading)

(defn wrap
  "Wrap an app such that before a request is passed to the app, each namespace
  identified by syms in reloadables is reloaded."
  ([reloadables app]
   (fn [env]
     (doseq [ns-sym reloadable-namespace-syms]
       (require ns-sym :reload))
     (app env)))
  ([reloadables]
   (partial wrap reloadables)))