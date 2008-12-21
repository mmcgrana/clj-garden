(ns clj-time.core-test
  (:use clj-unit.core clj-time.core))

(deftest "now"
  (prn (class (now)))
  (assert-instance org.joda.time.DateTime (now)))