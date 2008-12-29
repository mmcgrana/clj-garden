(ns cljurl.app.models-test
  (:use clj-unit.core cljurl.app.models stash.core))

(deftest "generate-slug"
  (let [[shortening success] (generate-slug {})]
    (assert-match #"[abcdefghijk12345]{5}" (:slug shortening))
    (assert-that success)))

(deftest "find-shortenings"
  (doseq [i (range 5)]
    (create +shortening+ {:url "http://google.com"}))
  (assert= 2 (count (find-recent-shortenings 2))))

