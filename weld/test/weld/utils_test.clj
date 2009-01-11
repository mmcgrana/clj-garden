(ns weld.utils-test
  (:use clj-unit.core weld.utils))

(deftest "re-without"
  (assert= "foobat" (re-without #"bar" "foobat"))
  (assert= "foobat" (re-without #"bar" "foobarbat"))
  (assert= "foobat" (re-without #"\d*" "foo123bat")))

(deftest "re-match?"
  (assert-that (re-match? #"foo" "foo"))
  (assert-that (re-match? #"o"   "foo"))
  (assert-not   (re-match? #"bar" "foo")))

(deftest "take-last"
  (assert= (list 3 4 5) (take-last 3 (list 1 2 3 4 5)))
  (assert= (list 1 2 3) (take-last 5 (list 1 2 3))))

(deftest "memoize-by"
  (let [memoized (memoize-by :mem-key :val-key)]
    (let [h1 {:mem-key 1 :val-key :a}
          h2 {:mem-key 1 :val-key :b}
          h3 {:mem-key 2 :val-key :c}]
      (assert= (memoized h1) :a)
      (assert= (memoized h1) :a)
      (assert= (memoized h2) :a)
      (assert= (memoized h3) :c))))