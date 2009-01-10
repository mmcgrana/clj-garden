(ns ring.middleware.show-exceptions-test
  (:use clj-unit.core ring.middleware.show-exceptions))

(def app (wrap #(throw (Exception. "fail"))))

(def html-env {})
(def js-env   {:headers {"accept" "text/javascript"}})

(deftest "wrap"
  (let [{:keys [status headers] :as response} (app html-env)]
    (assert= 500 status)
    (assert= {"Content-Type" "text/html"} headers))
  (let [{:keys [status headers]} (app js-env)]
    (assert= 500 status)
    (assert= {"Content-Type" "text/javascript"} headers)))
