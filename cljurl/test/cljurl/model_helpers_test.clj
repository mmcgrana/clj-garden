(ns cljurl.model-helpers-test
  (:use clj-unit.core stash.core (cljurl test-helpers models model-helpers)))

(deftest "inc-attr"
  (assert= 1
    (:hit_count (inc-attr (init* +hit+ {:hit_count 0}) :hit_count))))

(deftest "reload"
  (with-fixtures [fx]
    (let [hit     (fx :hits :on-1)
          inc-hit (inc-attr hit :hit_count)]
      (assert= (:hit_count hit) (:hit_count (reload inc-hit))))))