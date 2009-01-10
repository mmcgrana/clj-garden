(ns weld.app-test
  (:use clj-unit.core weld.app weld.request)
  (:require weld.routing-test))

(def base-env {:request-method :get :uri "/show/foo"})

(deftest "spawn-app"
  (let [app (spawn-app weld.routing-test/router)
        req (app base-env)]
    (assert= {:slug "foo"} (params req))
    (assert= :get          (request-method req))))

