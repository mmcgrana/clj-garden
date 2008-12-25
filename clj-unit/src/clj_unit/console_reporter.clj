(ns clj-unit.console-reporter
  (:use clj-unit.utils clj-backtrace.repl))

; composed of tests
; a test may be pending or not pending
; if it is pending it is nothing else
; if it is not pending it has 0 or more assertions
; if a test has n assertions between 0 and n assertions may be invoked
; if assertion is invoked without error it either succeeds or fails
; an error may occur before, after, or during any assertions within a test
; a test either passes, failes, or errors.
; # asserts as success + fails is incorrect but so is + success + fails + errors
; may need more general assert-that helper

(def *test-count*    (atom 0))
(def *success-count* (atom 0))
(def *pass-count*    (atom 0))
(def *failure-count* (atom 0))
(def *error-count*   (atom 0))
(def *pending-count* (atom 0))
(def *start-time*    (atom nil))

(defn print-exception
  "A sort of modified .printStackTrace. Prints to *out* as apposed to *err* and
  only prints those stack elements above the test invocation code."
  [#^Exception e]
  (let [elems      (.getStackTrace e)
        ours?      (fn [#^StackTraceElement m]
                     (re-match? #"clj_unit.core\$run_tests" (.getClassName m)))
        user-elems (take-while #(not (ours? %)) elems)]
    (println (str e))
    (print-trace user-elems)))

(def +console-reporter+
  {:init
    (fn []
      {:test-count    0
       :success-count 0
       :pass-count    0
       :failure-count 0
       :error-count   0
       :pending-count 0
       :start-time    nil})
   :start
     (fn [state ns-sym]
       (printf "\nTesting: %s\n" ns-sym)
       (assoc state :start-time (System/currentTimeMillis)))
   :test
     (fn [state test-info]
       (if (:pending test-info)
         (do
           (printf "\nPEND: %s\n" (:doc test-info))
           (update state :pending-count inc))
         (update state :test-count inc)))
   :success
     (fn [state test-info]
       (print ".") (flush)
       (update state :success-count inc))
   :pass
    (fn [state test-info]
      (update state :pass-count inc))
   :failure
    (fn [state test-info message]
      (printf "\nFAIL: %s (%s:%s)\n"
        message (:file test-info) (:line test-info))
      (update state :failure-count inc))
   :error
    (fn [state test-info #^Exception e]
      (printf "\nEXCP: %s (%s:%s)\n"
        (:doc test-info) (:file test-info) (:line test-info))
      (print-exception e)
      (update state :error-count inc))
   :end
     (fn [state ns-sym]
       (println)
       (printf "%s tests, %s assertions (%.3f secs)\n"
         (:test-count state)
         (+ (:success-count state) (:failure-count state))
         (float (/ (- (System/currentTimeMillis) (:start-time state)) 1000)))
       (printf "%s failures, %s erros, %s pending\n"
         (:failure-count state) (:error-count state) (:pending-count state))
       (println)
       state)
   :no-tests
     (fn [state ns-sym]
       (printf "No tests for %s" ns-sym)
       state)})