(ns cljurl.models-test
  (:use clj-unit.core stash.core
        (cljurl models model-helpers test-helpers)))

(deftest "generate-slug"
  (let [[shortening success] (generate-slug {})]
    (assert-match #"[abcdefghijk12345]{5}" (:slug shortening))
    (assert-that success)))

(deftest "find-shortenings"
  (doseq [i (range 5)]
    (create +shortening+ {:url "http://google.com"}))
  (assert= 2 (count (find-recent-shortenings 2))))

(deftest "hit-shortening: existing ip"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          ht (fx :hits :on-1)]
      (hit-shortening sh (:ip ht))
      (assert= (inc (:hit_count ht))
        (:hit_count (reload ht))))))

(deftest "hit-shortening: new ip"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)]
      (hit-shortening sh "new")
      (assert= 1
        (:hit_count (find-one +hit+
                      {:where [:and [:ip := "new"]
                                    [:shortening_id := (:id sh)]]}))))))