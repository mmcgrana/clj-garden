(ns weld.app
  (:use weld.request weld.routing))

(defn spawn-app
  "Returns an app paramaterized by the given router, as compiled by
  weld.routing/compiled-router."
  [router]
  (fn [env]
    (let [[act-fn r-params] (recognize router (request-method* env) (uri env))
          env+           (assoc-route-params env r-params)]
      (act-fn env+))))
