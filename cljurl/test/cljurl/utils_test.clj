(ns cljurl.utils-test
  (:use clj-unit.core cljurl.utils))

(deftest "str-cat"
  (assert= ""       (str-cat (list)))
  (assert= "foobar" (str-cat (list "foo" "bar"))))

(deftest "choice"
  (choice ['a 'b 'c 'd]))