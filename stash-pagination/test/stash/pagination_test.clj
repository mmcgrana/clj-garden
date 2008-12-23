(ns stash.pagination-test
  (:use clj-unit.core stash.pagination))

(def elems  '[a b c d e f g h i j k l m])
(def elems2 '[a b c d e f])
(def elems3 '[a b])

(defn pager-for
  [pg per-pg elms]
  (pager pg per-pg
    (fn [p] (count elms))
    (fn [p] (subvec elms
              (offset p)
              (min (count elms) (+ (offset p) (per-page p)))))))

(def pager-basic  (pager-for 2 3 elems))
(def pager-exact  (pager-for 2 3 elems2))
(def pager-single (pager-for 1 3 elems3))
(def pager-tail   (pager-for 5 3 elems))
(def pager-low    (pager-for 0 3 elems))
(def pager-high   (pager-for 3 3 elems2))

(deftest "pager"
  (assert-fn map? pager-basic)
  (assert-fn map? pager-low))

(deftest "page"
  (assert= 2 (page pager-basic)))

(deftest "per-page"
  (assert= 3 (per-page pager-basic)))

(deftest "total-entries"
  (assert= 13 (total-entries pager-basic))
  (assert= 13 (total-entries pager-low)))

(deftest "entries"
  (assert= '[d e f] (entries pager-basic))
  (assert= '[d e f] (entries pager-exact))
  (assert= '[a b]   (entries pager-single))
  (assert= '[m]     (entries pager-tail)))

(deftest "total-pages"
  (assert= 5  (total-pages pager-basic))
  (assert= 2  (total-pages pager-exact))
  (assert= 1  (total-pages pager-single))
  (assert= 5  (total-pages pager-tail)))

(deftest "out-of-bounds?"
  (assert-not-fn out-of-bounds? pager-basic)
  (assert-not-fn out-of-bounds? pager-exact)
  (assert-not-fn out-of-bounds? pager-single)
  (assert-not-fn out-of-bounds? pager-tail)
  (assert-fn     out-of-bounds? pager-low)
  (assert-fn     out-of-bounds? pager-high))

(deftest "offset"
  (assert= 3 (offset pager-basic))
  (assert= 0 (offset pager-single)))

(deftest "next-page"
  (assert= 3  (next-page pager-basic))
  (assert-nil (next-page pager-exact))
  (assert-nil (next-page pager-single))
  (assert-nil (next-page pager-tail)))

(deftest "previous-page"
  (assert= 1  (previous-page pager-basic))
  (assert= 1  (previous-page pager-exact))
  (assert-nil (previous-page pager-single))
  (assert= 4  (previous-page pager-tail)))

