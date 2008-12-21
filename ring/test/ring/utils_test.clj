(ns ring.utils-test
  (:use clj-unit.core ring.utils))

(deftest "re-without"
  (assert= "foobat" (re-without #"bar" "foobat"))
  (assert= "foobat" (re-without #"bar" "foobarbat"))
  (assert= "foobat" (re-without #"\d*" "foo123bat")))

(deftest "re-matches?"
  (assert-truth (re-matches? #"foo" "foo"))
  (assert-truth (re-matches? #"o"   "foo"))
  (assert-not   (re-matches? #"bar" "foo")))

(deftest "take-last"
  (assert= (list 3 4 5) (take-last 3 (list 1 2 3 4 5)))
  (assert= (list 1 2 3) (take-last 5 (list 1 2 3))))
