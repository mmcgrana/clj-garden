(ns clj-unit.core
  (:use (clj-unit utils console-reporter) clojure.contrib.except))

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
  (let [is-pending (empty? body)]
    `(let [ns-sym#        (.name *ns*)
           fn#            (fn [] ~@body)
           [file#, line#] (file-line)
           test-info#     (struct +test-info+ ~doc ~is-pending fn# file# line#)]
       (swap! *tests-info*
         (fn [tests-info#]
           (update tests-info# ns-sym#
             (fn [tests#] (conj (or tests# []) test-info#))))))))

(defn run-tests
  "Run all tests for the namespace symbols, with either the default console
  reporter or if given a custom reporter."
  [ns-syms & [reporter]]
  (binding [*reporter* (or reporter +console-reporter+)]
    (doseq [ns-sym ns-syms]
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
                    ((*reporter* :error) *test-info* e))))))
          ((*reporter* :end) ns-sym))
        ((*reporter* :no-tests) ns-sym)))))

(defn success
  "Report a successfull assertion."
  []
  ((*reporter* :success) *test-info*))

(defn failure
  "Report a failed assertion, with a message indicating the reason."
  [message]
  ((*reporter* :failure) *test-info* message))

(load "core_assertions")