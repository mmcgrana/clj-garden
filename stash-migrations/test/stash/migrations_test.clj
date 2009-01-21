(ns stash.migrations-test
  (:require [clj-jdbc.core :as jdbc])
  (:use clj-unit.core stash.migrations clj-jdbc.data-sources)
  (:load "migrations_ddl_test"))

(deftest-conn "ensure-version, get-version, set-version, drop_version"
  (ensure-version)
  (ensure-version)
  (assert= 0 (get-version))
  (set-version 1)
  (assert= 1 (get-version))
  (drop-version))

(def u1 #(constantly :u1))
(def u3 #(constantly :u3))
(def u5 #(constantly :u5))
(def u7 #(constantly :u7))

(def d1 #(constantly :d1))
(def d3 #(constantly :d3))
(def d5 #(constantly :d5))
(def d7 #(constantly :d7))

(def +migrations+
  [[1 u1 d1] [3 u3 d3] [5 u5 d5] [7 u7 d7]])

(deftest "ups"
  (assert= [[1 u1 1] [3 u3 3] [5 u5 5] [7 u7 7]]
    (ups +migrations+ 0 7))
  (assert= [[3 u3 3] [5 u5 5]]
    (ups +migrations+ 1 5)))

(deftest "downs"
  (assert= [[7 d7 5] [5 d5 3] [3 d3 1] [1 d1 0]]
    (downs +migrations+ 7 0))
  (assert= [[5 d5 3] [3 d3 1]]
    (downs +migrations+ 5 1)))

;(deftest-conn "migrate: up, down, no migrations"
;  (ensure-version)
;  (assert-nil (migrate +migrations+ 0))
;  (assert= 0 (get-version))
;  (migrate +migrations+ 7)
;  (assert= 7 (get-version))
;  (migrate +migrations+ 0)
;  (assert= 0 (get-version))
;  (drop-version))
;
;(deftest "migrate-with"
;  (let [logger {:test (constantly true) :log identity}]
;    (migrate-with +migrations+ 7 +data-source+ nil))
;  (jdbc/with-connection +data-source+ (drop-version)))
;
;(deftest "defmigration"
;  (defmigration my-migration 10
;    :up
;    :down)
;  (assert= 10 (get my-migration 0))
;  (assert= :up ((get my-migration 1)))
;  (assert= :down ((get my-migration 2))))
