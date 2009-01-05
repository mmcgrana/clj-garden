(ns cwsg.middleware.reloading)

(defn wrap [find-reloadable-namespaces app]
  "Wrap an app such that before a request is passed to the app, the
  find-reloadable-namespaces fn is called and for every symbol in the coll 
  returned by that fn the corresponding namespace is reloaded."
  (fn [env]
    (doseq [ns-sym (find-reloadable-namespaces)]
      (require ns-sym :reload))
    (app env)))