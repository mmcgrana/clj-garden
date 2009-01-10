(ns clj-time.core-test
  (:use clj-unit.core clj-time.core)
  (:import (org.joda.time DateTime)))

(deftest "now"
  (assert-instance DateTime (now)))

(deftest "zero"
  (let [t (zero)]
    (assert-instance DateTime t)
    (assert= 0 (.getMillis t))))

(deftest "xmlschema"
  (assert-match #"\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d\.\d\d\dZ" (xmlschema (now))))