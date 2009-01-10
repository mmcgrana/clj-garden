(ns weld.app-test
  (:use clj-unit.core
        (weld app request))
  (:require weld.routing-test))

(defn str-input-stream
  "Returns a ByteArrayInputStream for the given String."
  [string]
  (java.io.ByteArrayInputStream. (.getBytes string)))

(def base-env {:request-method :get :uri "/show/foo"
               :body (str-input-stream   "foobar")})

(deftest "spawn-app"
  (let [app (spawn-app weld.routing-test/router)
        env (app base-env)]
    (assert= {:slug "foo"} (params env))
    (assert= :get          (request-method env))
    (assert= "foobar"      (body-str env))))

