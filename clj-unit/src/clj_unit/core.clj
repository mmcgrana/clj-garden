(ns clj-unit.core
  (:use (clj-unit utils console-reporter)
        (clojure.contrib except)))

(defstruct +test-info+ :doc :pending :fn :file :line)

(def *tests-info* (atom {}))

(declare *test-reporter* *test-reporter-state* *test-info*)

; reporter interface
; hash with keys valued by the functions with sigs as below
; each function (except init) takes state as its first argument and should
; return the updated state, which it will then be passed back on the next
; report call, etc.
; 
; :init     <ns-syms>                     return starting state
; :start    <state ns-sym>                start of namespace test suite
; :test     <state test-info>             start of 1 test, note possibly :pending
; :success  <state test-info>             assertion within a test succeeded
; :failure  <state test-info> <message>   assertion within a test failed
; :pass     <state test-info>             1 test passsed without error or failure
; :error    <state test-finfo> <thrown>   uncaught error during test
; :finish   <state ns-sym>                end of namespace test suite
; :end      <state>                       end of all test suites
; :no-tests <state ns-sym>                no-tests for namespace

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

(defn report
  "Invoke the reporter function corresponding to the type keyword with the
  current reporter state and any additional args, setting the reporter
  state to the updated value returned by the fn."
  [type & args]
  (set! *test-reporter-state*
    (apply (type *test-reporter*) *test-reporter-state* args)))

(defn run-tests
  "Run all tests for the namespace symbols, with either the default console
  reporter or if given a custom reporter."
  [& ns-syms]
  (binding [*test-reporter* +console-reporter+]
    (binding [*test-reporter-state* ((:init *test-reporter*) ns-syms)]
      (doseq [ns-sym ns-syms]
        (if-let [ns-tests-info (@*tests-info* ns-sym)]
          (do
            (report :start ns-sym)
            (doseq [test-info ns-tests-info]
              (binding [*test-info* test-info]
                (report :test *test-info*)
                (if-not (*test-info* :pending)
                  (try
                    ((*test-info* :fn))
                    (report :pass *test-info*)
                    (catch Exception e
                      (report :error *test-info* e))))))
            (report :finish ns-sym))
          (report :no-tests ns-sym)))
      (report :end))))

(defn require-and-run-tests
  "Like run-tests, but require all namespaces before running all of the tests"
  [& ns-syms]
  (doseq [ns-sym ns-syms] (require ns-sym))
  (apply run-tests ns-syms))

(defn success
  "Report a successfull assertion."
  []
  (report :success *test-info*))

(defn failure
  "Report a failed assertion, with a message indicating the reason."
  [message]
  (report :failure *test-info* message))

(load "core_assertions")