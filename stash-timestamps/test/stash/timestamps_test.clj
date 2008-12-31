(ns stash.timestamps-test
  (:require [clj-time.core :as time])
  (:use clj-unit.core stash.timestamps))

(deftest "timestamp-create"
  (let [t (time/now)]
    (binding [time/now (fn [] t)]
      (assert=
        [{:created_at t :updated_at t :other nil} true]
        (timestamp-create
          {:created_at nil :updated_at nil :other nil})))))

(deftest "timestamp-create"
  (let [t (time/now)]
    (binding [time/now (fn [] t)]
      (assert=
        [{:created_at :created :updated_at t :other nil} true]
        (timestamp-update
          {:created_at :created :updated_at :updated :other nil})))))