(ns ring.endpoints.dump-test
  (:use clj-unit.core ring.endpoints.dump)
  (:import java.io.ByteArrayInputStream))

(def post-env
  {:uri            "/foo/bar"
   :request-method :post
   :body           (ByteArrayInputStream. (.getBytes "post body"))})

(def get-env
  {:uri            "/foo/bar"
   :request-method :get})

(deftest "app"
  (let [{:keys [status]} (app post-env)]
    (assert= 200 status))
  (let [{:keys [status]} (app get-env)]
    (assert= 200 status)))
