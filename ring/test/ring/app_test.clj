(ns ring.app-test
  (:use clj-unit.core ring.app ring.request))

(def env {:request-method :get})

(def r-params {:route "params"})

(def recognizer (fn [method uri] [(fn [r] r) r-params]))

(deftest "spawn-app"
  (let [app (spawn-app recognizer)
        req (app env)]
    (assert= r-params (params req))
    (assert= :get     (request-method req))))
