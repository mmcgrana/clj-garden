(ns stash.migrations-test
  (:require [clj-jdbc.core :as jdbc])
  (:use clj-unit.core stash.migrations clj-jdbc.data-sources)
  (:load "migrations_ddl_test"))

(deftest-conn "create-version, get-version, set-version, drop_version" [conn]
  (create-version conn)
  (assert= 0 (get-version conn))
  (set-version conn 1)
  (assert= 1 (get-version conn))
  (drop-version conn))

(def +migrations+
  [[1 identity] [3 identity] [5 identity] [7 identity]])

(deftest "ups"
  (assert= [[1 identity 1] [3 identity 3] [5 identity 5] [7 identity 7]]
    (ups +migrations+ 0 7))
  (assert= [[3 identity 3] [5 identity 5]]
    (ups +migrations+ 1 5)))

(deftest "downs"
  (assert= [[7 identity 5] [5 identity 3] [3 identity 1] [1 identity 0]]
    (downs +migrations+ 7 0))
  (assert= [[5 identity 3] [3 identity 1]]
    (downs +migrations+ 5 1)))

(deftest-conn "migrate: up, down, no migrations" [conn]
  (create-version conn)
  (assert-nil (migrate conn +migrations+ 0))
  (assert= 0 (get-version conn))
  (assert= '(1 3 5 7) (migrate conn +migrations+ 7))
  (assert= 7 (get-version conn))
  (assert= '(7 5 3 1) (migrate conn +migrations+ 0))
  (assert= 0 (get-version conn))
  (drop-version conn))

(deftest "defmigration"
  (defmigration my-migration [conn 10]
    [:up conn]
    [:down conn])
  (assert= 10 (get my-migration 0))
  (assert= [:up   :conn] ((get my-migration 1) :conn))
  (assert= [:down :conn] ((get my-migration 2) :conn)))