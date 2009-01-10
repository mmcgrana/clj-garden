(ns ring.middleware.reloading-test
  (:use clj-unit.core ring.middleware.reloading))

(def app (constantly :response))

(deftest "wrap"
  (let [wrapped (wrap '(ring.middleware.reloading) app)]
    (assert= :response (wrapped :request)))
  (let [curried (wrap '(ring.middleware.reloading))
        wrapped (curried app)]
    (assert= :response (wrapped :request))))