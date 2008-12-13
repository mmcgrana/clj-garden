; deftest
; assert=
; assert-is
; assert-not
; assert-nil
; pending

; test-ns

; print results

; assert [1 2 3] (comp + 1) [2 3 4]

; return value based or not???
; perhaps if can return an assertion or a seq-able of assertions

; possibilities
; * compile-time error
; * exception before assertion
; * exception in inner assertion forms
; * returns non-assertion
; * returns a failing assertion
; * returns a passing assertion

; do we defintely need non-return value based assertions, can we get away with
; not having those??
; transaction testing
; convience w/ many asserts
; story-type tests
; would really like a way around this.
; 2 big cases: lots of assertions, and dealing with stateful systems (webapps)

; (deftest "transaction"
;   (in-trans
;     (make-change)
;     (assert-changed)
;     (rollback))
;   (assert-gone))

; note current problem with failing - it doesn't stop after 2 examples if 2
; of 3 fails. note that escaped exception works as desired.

(ns clj-unit.core
  (:use (clj-unit failure-exception console-reporter) clojure.contrib.except))

(defstruct +test-info+ :doc :test-fn)

(def *tests-info* (atom {}))

(declare *reporter*)

(defmacro deftest
  "Define a unit test."
  [doc & body]
  (let [test-fn (if (empty? body) nil `(fn [] ~@body))]
    `(let [ns-sym# (.name *ns*)
           info#   (struct +test-info+ ~doc ~test-fn)]
      (swap! *tests-info*
        (fn [tests-info#]
          (assoc tests-info# ns-sym# (conj (tests-info# ns-sym# []) info#)))))))

(defn run-tests
  "Run all tests for the namespace identified by the given sym."
  [ns-sym & [reporter]]
  (if-let [ns-tests-info (@*tests-info* ns-sym)]
    (binding [*reporter* (or reporter +console-reporter+)]
      (do
        ((*reporter* :start) ns-sym)
        (doseq [{:keys [doc test-fn]} ns-tests-info]
          ((*reporter* :test))
          (if test-fn
            (try
              (test-fn)
              ((*reporter* :pass) doc)
              (catch clj-unit.FailureException e
                ((*reporter* :failure) doc (.getMessage e)))
              (catch Exception e
                ((*reporter* :error) doc e)))
            ((*reporter* :pending) doc)))
      ((*reporter* :end))))
    ((*reporter* :no-tests) ns-sym)))

(defn success
  "Report a successfull assertion."
  []
  ((*reporter* :success)))

(defn failure
  "Report a failed assertion, with a message indicating the reason."
  [message]
  (throwf clj-unit.FailureException message))

(load "core_assertions")