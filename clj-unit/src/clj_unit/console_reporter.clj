(ns clj-unit.console-reporter)

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

(defn print-stack-trace
  "A sort of modified .printStackTrace. Prints to *out* as apposed to *err* and
  only prints those stack elements above the test invocation code."
  [#^Exception e]
  (let [elems      (seq (.getStackTrace e))
        ours?      (fn [#^StackTraceElement m]
                     (re-find #"clj_unti_T_" (.getClassName m)))
        user-elems (take-while #(not (ours? %)) elems)]
    (println (str e))
    (doseq [user-elem user-elems]
      (println (str "  " user-elem)))))

(def +console-reporter+
  {:start
     (fn [ns-sym]
       (swap! *start-time* (fn [v] (System/currentTimeMillis)))
       (printf "\nTesting: %s\n" ns-sym))
   :test
     (fn [test-info]
       (if (:pending test-info)
         (do
           (swap! *pending-count* inc)
           (printf "\nPEND: %s\n" (:doc test-info)))
         (swap! *test-count* inc)))
   :success
     (fn [test-info]
       (swap! *success-count* inc)
       (print ".") (flush))
   :pass
    (fn [test-info]
      (swap! *pass-count* inc))
   :failure
    (fn [test-info message]
      (swap! *failure-count* inc)
      (printf "\nFAIL: %s (%s:%s)\n"
        message (:file test-info) (:line test-info)))
   :error
    (fn [test-info #^Exception e]
      (swap! *error-count* inc)
      (printf "\nEXCP: %s (%s:%s)\n"
        (:doc test-info) (:file test-info) (:line test-info))
      (print-stack-trace e))
   :end
     (fn [ns-sym]
       (println)
       (printf "%s tests, %s assertions (%.3f secs)\n"
         @*test-count*
         (+ @*success-count* @*failure-count*)
         (float (/ (- (System/currentTimeMillis) @*start-time*) 1000)))
       (printf "%s failures, %s erros, %s pending\n"
         @*failure-count* @*error-count* @*pending-count*)
       (println))
   :no-tests
     (fn [ns-sym]
       (printf "No tests for %s" ns-sym))})