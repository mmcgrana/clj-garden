(ns stash.utils-test
  (:use clj-unit.core stash.utils))


(deftest "update"
  (assert=
    {:foo 2}
    (update {:foo 1} :foo inc))
  (assert=
    {:foo 6}
    (update {:foo 1} :foo + 2 3)))

(deftest "mash"
  (doseq [f [list vector]]
    (assert=
      {:foo "foo" :bar "bar"}
      (mash (fn [k] (f k (name k))) (list :foo :bar)))))

(deftest "with-assoc-meta"
  (assert=
    (with-meta {:foo :bar} {:biz :bat :whiz :bang})
    (with-assoc-meta (with-meta {:foo :bar} {:biz :bat}) :whiz :bang)))

(deftest "update-meta-by"
  (assert=
    (with-meta {:foo :bar} {:biz "BAT"})
    (update-meta-by
      (with-meta {:foo :bar} {:biz "bat"})
      :biz (memfn toUpperCase))))

(deftest "def-"
  (assert-truth (:private (meta (def- foo "bar")))))

(deftest "get-or"
  (assert= :bar (get-or {:foo :bar} :foo (throw (Exception. "fail"))))
  (assert= :bat (get-or {:foo :bar} :biz :bat)))

(deftest "limit-keys"
  (let [m {:foo :bar :biz :bat}]
    (assert= m (limit-keys m [:foo :biz :whiz])))
  (assert-throws #"Unrecognized keys \(:biz\)"
    (limit-keys {:foo :bar :biz :bat} [:foo :whiz])))

(deftest "the-str"
  (assert= "foo" (the-str :foo))
  (assert= "foo" (the-str "foo")))

(deftest "re-match?"
  (assert-truth (re-match? #"f.o" "foo"))
  (assert-truth (re-match? #"f.o" "foobar"))
  (assert-not   (re-match? #"b.r" "foo")))