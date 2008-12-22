(ns clj-time.core-test
  (:use clj-unit.core clj-time.core))

(deftest "now"
  ; TODO: test for utc zone
  (assert-instance org.joda.time.DateTime (now)))