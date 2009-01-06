(ns ring.app-test
  (:use clj-unit.core ring.app ring.request)
  (:require ring.routing-test))

(def env {:request-method :get :uri "/show/foo"})

(deftest "spawn-app"
  (let [app (spawn-app ring.routing-test/router)
        req (app env)]
    (assert= {:slug "foo"} (params req))
    (assert= :get          (request-method req))))

(deftest "wrap-if"
  (let [core-app   inc
        wrapper-fn (fn [app] (fn [env] (+ 2 (app env))))
        unwrapped  (wrap-if false wrapper-fn core-app)
        wrapped    (wrap-if true  wrapper-fn core-app)]
    (assert= 1 (unwrapped 0))
    (assert= 3 (wrapped 0))))
