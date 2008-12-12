(ns cljurl.app.models-test
  (:use clj-unit.core)
  (:require [cljurl.app.models :as m]))

(deftest "next-slug returns the next available slug"
  (assert= "abad" (m/next-slug "abac")))

(deftest "next-slug increases chars other than the last"
  (assert= "abba" (m/next-slug "abad")))

(deftest "next-slug increase slug length as neccessary"
  (assert= "aaaa" (m/next-slug "ddd")))

(deftest "find-prev-slug returns the last slug in lexographic order"
  (in-transaction
    ))

; note that next-slug is actually broken
; others should be part of model framework
; btw models will need proper declarations
; need to create proper model abstractions
