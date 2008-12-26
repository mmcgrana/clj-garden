(ns ring.app-test
  (:use clj-unit.core ring.app ring.request)
  (:require ring.routing-test))

(def env {:request-method :get :uri "/show/foo"})

(deftest "spawn-app"
  (let [app (spawn-app ring.routing-test/router)
        req (app env)]
    (assert= {:slug "foo"} (params req))
    (assert= :get          (request-method req))))
