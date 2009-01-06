(ns cwsg.middleware.reloading)

(defn wrap [reloadable-namespace-syms app]
  "Wrap an app such that before a request is passed to the app, each namespace
  identified by reloadable-namespace-syms is reloaded."
  (fn [env]
    (doseq [ns-sym reloadable-namespace-syms]
      (require ns-sym :reload))
    (app env)))