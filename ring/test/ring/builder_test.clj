(ns ring.builder-test
  (:use clj-unit.core ring.builder))

(deftest "wrap-if"
  (let [core-app inc
        wrapper-fn (fn [app] (fn [env] (+ 2 (app env))))
        unwrapped (wrap-if false wrapper-fn core-app)
        wrapped (wrap-if true wrapper-fn core-app)]
    (assert= 1 (unwrapped 0))
    (assert= 3 (wrapped 0))))