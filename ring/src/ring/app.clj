(ns ring.app
  (:use ring.request ring.routing))

(defn spawn-app
  "Returns an app paramaterized by the given router, as compiled by
  ring.routing/compiled-router."
  [router]
  (fn [env]
    (let [req                 (from-env env)
         [action-fn r-params] (recognize router (request-method req) (uri req))
         request              (assoc-route-params req r-params)]
      (action-fn request))))

(defn wrap-if
  "If test is logically true, returns the result of invoking the wrapper on the 
  core app, i.e. a wrapped app; if test is logically false, returns the core
  app."
  [test wrapper-fn core-app]
  (if test (wrapper-fn core-app) core-app))