(ns weld.app-test
  (:use clj-unit.core
        (weld app request self-test-helpers))
  (:require weld.routing-test))

(deftest "spawn-app"
  (let [app       (spawn-app weld.routing-test/router)
        echod-req (app (req-with {:request-method :get :uri "/show/foo"
                                  :body (str-input-stream   "foobar")}))]
    (assert= {:slug "foo"} (params echod-req))
    (assert= :get          (request-method echod-req))
    (assert= "foobar"      (body-str echod-req))))

