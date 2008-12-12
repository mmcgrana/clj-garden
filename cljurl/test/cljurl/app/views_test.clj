(ns cljurl.app.views-test
  (:use clj-unit.core)
  (:require [cljurl.app.views :as v]))

(deftest "layout works"
  (assert-is
    (v/layout {} "content")))

(deftest "multi test"
  (let [b (v/index)]
    (assert-selector b "foo > #bar")
    (assert-selector b "biz > .bat")))

(deftest "index works"
  (assert-is
    (v/index {})))

(deftest "show works"
  (assert-is
    (v/show {:shortening {:url "http://google.com" :slug "abc"}})))

(deftest "not-found works"
  (assert-is
    (v/not-found {})))