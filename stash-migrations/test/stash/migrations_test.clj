(ns stash.migrations-test
  (:require [clj-jdbc.core :as jdbc])
  (:use clj-unit.core stash.migrations clj-jdbc.data-sources)
  (:load "migrations_ddl_test"))

(deftest-conn "create-version, get-version, set-version, drop_version"
  (create-version)
  (assert= 0 (get-version))
  (set-version 1)
  (assert= 1 (get-version))
  (drop-version))

(def m1 #(constantly :1))
(def m3 #(constantly :3))
(def m5 #(constantly :5))
(def m7 #(constantly :7))

(def +migrations+
  [[1 m1] [3 m3] [5 m5] [7 m7]])

(deftest "ups"
  (assert= [[1 m1 1] [3 m3 3] [5 m5 5] [7 m7 7]]
    (ups +migrations+ 0 7))
  (assert= [[3 m3 3] [5 m5 5]]
    (ups +migrations+ 1 5)))

(deftest "downs"
  (assert= [[7 m7 5] [5 m5 3] [3 m3 1] [1 m1 0]]
    (downs +migrations+ 7 0))
  (assert= [[5 m5 3] [3 m3 1]]
    (downs +migrations+ 5 1)))

(deftest-conn "migrate: up, down, no migrations"
  (create-version)
  (assert-nil (migrate +migrations+ 0))
  (assert= 0 (get-version))
  (assert= '(1 3 5 7) (migrate +migrations+ 7))
  (assert= 7 (get-version))
  (assert= '(7 5 3 1) (migrate +migrations+ 0))
  (assert= 0 (get-version))
  (drop-version))

(deftest "defmigration"
  (defmigration my-migration 10
    :up
    :down)
  (assert= 10 (get my-migration 0))
  (assert= :up ((get my-migration 1)))
  (assert= :down ((get my-migration 2))))
