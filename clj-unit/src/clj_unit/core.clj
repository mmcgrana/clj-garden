(ns clj-unit.core
  (:use (clj-unit console-reporter) clojure.contrib.except))

(defstruct +test-info+ :doc :pending :fn :file :line)

(def *tests-info* (atom {}))

(declare *reporter* *test-info*)


; reporter interface
; hash with keys valued by the functions with sigs as below
; 
; :start    <ns-sym>                start of namespace test suite
; :test     <test-info>             start of 1 test, note possibly :pending
; :success  <test-info>             assertion within a test succeeded
; :failure  <test-info> <message>   assertion within a test failed
; :pass     <test-info>             1 test passsed without error or failure
; :error    <test-finfo> <thrown>   uncaught error during test
; :end      <ns-sym>                end of namespace test suite
; :no-tests <ns-sym>                no-tests for namespace, called exclusively


(defmacro deftest
  "Define a unit test."
  [doc & body]
  (let [temp-sym   (gensym "clj-unti-T-")
        is-pending (empty? body)]
    `(do
        (def ~temp-sym (fn [] ~@body))
        (let [ns-sym#    (.name *ns*)
              test-meta# (meta (var ~temp-sym))
              test-file# (:file test-meta#)
              test-line# (:line test-meta#)
              test-info# (struct +test-info+
                           ~doc ~is-pending ~temp-sym test-file# test-line#)]
          (swap! *tests-info*
            (fn [tests-info#]
              (assoc tests-info# ns-sym#
                (conj (get tests-info# ns-sym# []) test-info#))))))))

(defn test-failure-exception?
  "Returns true if this exception was thrown from our failure message to
  indicate that execution for this test should stop."
  [e]
  (let [top-elem  (aget (.getStackTrace e) 0)
        top-cname (.getClassName top-elem)]
    (.startsWith top-cname "clj_unit.core$failure")))

(defn run-tests
  "Run all tests for the namespace identified by the given sym."
  [ns-sym & [reporter]]
  (binding [*reporter* (or reporter +console-reporter+)]
    (if-let [ns-tests-info (@*tests-info* ns-sym)]
      (do
        ((*reporter* :start) ns-sym)
        (doseq [test-info ns-tests-info]
          (binding [*test-info* test-info]
            ((*reporter* :test) *test-info*)
            (if-not (*test-info* :pending)
              (try
                ((*test-info* :fn))
                ((*reporter* :pass) *test-info*)
                (catch Exception e
                  (if-not (test-failure-exception? e)
                    ((*reporter* :error) *test-info* e)))))))
        ((*reporter* :end) ns-sym))
      ((*reporter* :no-tests) ns-sym))))

(defn success
  "Report a successfull assertion."
  []
  ((*reporter* :success) *test-info*))

; need to throw abort exception here, catch above as before
(defn failure
  "Report a failed assertion, with a message indicating the reason."
  [message]
  ((*reporter* :failure) *test-info* message)
  (throwf "test failure"))

(load "core_assertions")