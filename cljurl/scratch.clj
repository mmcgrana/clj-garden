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


(ns cljurl.app.controllers-test
  (:use clj-unit.core)
  (:require [cljurl.app.controllers :as c]
            [cljurl.app.views :as v]
            [cljurl.app.models :as m
             cljurl.sql :as sql]))

(deftest "index works"
  (assert-is
    (v/index {})))

; note: we may be able to avoid needing multiple asserts / in-middle asserts
; be creating a helper function like do-create or something and than 
; wrapping calls to that function in 2 or 3 assertions

(deftest "rediects to existing shortening show page if already shortened"
  (sql/in-transaction
    (m/create-shortening {:slug "foo" :url "http://google.com"})
    (assert-unchanged #(m/count-shortenings)
      (assert-redirects
        (path :show {:slug "foo"})
        (c/create {:params {:url "http://google.com"}})))))

(deftest "creates shortening and redirects to its show page"
  (sql/in-transaction
    (m/create-shortening {:slug "a" :url "http://google.com"})
    (m/create-shortening {:slug "b" :url "http://yahoo.com"})
    ; create
    ; assert created
    ; assert redirects
    (assert-created??)))