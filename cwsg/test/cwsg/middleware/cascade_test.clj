(ns 'cwsg.middleware.cascade-test
  (:require cwsg.middleware.cascade)
  (:use clj-unit.core
        [clojure.contrib.def :only (defvar-)]))

(defvar- app1 (fn [env] [404 {"X-App-ID" "1"} "App 1"]))
(defvar- app2 (fn [env] [200 {"X-App-ID" "2"} "App 2"]))
(defvar- app3 (fn [env] [404 {"X-App-ID" "3"} "App 3"]))

(deftest "Returns first non-caught response from the given apps."
  (let [cascade (cwsg.cascade/with [app1 app2 app3] #{304 404})]
    (assert=
      [200 {"X-App-ID" "2"} "App 2"]
      (cascade {}))))