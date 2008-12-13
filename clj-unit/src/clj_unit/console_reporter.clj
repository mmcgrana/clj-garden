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

(defn print-stack-trace
  "A sort of modified .printStackTrace. Prints to *out* as apposed to *err* and
  only prints those stack elements above the test invocation code."
  [#^Exception e]
  (let [elems      (seq (.getStackTrace e))
        ours?      (fn [#^StackTraceElement m]
                     (.startsWith (.getClassName m) "clj_unit.core$run_test"))
        user-elems (take-while #(not (ours? %)) elems)]
    (println (str e))
    (doseq [user-elem user-elems]
      (println (str "  " user-elem)))))

(def +console-reporter+
  {:start
     (fn [ns-sym]
       (printf "\nTesting: %s\n" ns-sym))
   :test
     (fn []
       (swap! *test-count* inc))
   :success
     (fn []
       (swap! *success-count* inc)
       (print ".") (flush))
   :pass
    (fn [doc]
      (swap! *pass-count* inc))
   :failure
    (fn [doc message]
      (swap! *failure-count* inc)
      (printf "\nFAIL: %s\n  %s\n" doc message))
   :error
    (fn [doc #^Exception e]
      (swap! *error-count* inc)
      (printf "\nEXCP: %s\n" doc)
      (print-stack-trace e))
   :pending
     (fn [doc]
       (swap! *pending-count* inc)
       (printf "\nPEND: %s\n" doc))
   :end
     (fn []
       (println) (println)
       (printf "%s tests, %s assertions\n"
         @*test-count* (+ @*success-count* @*failure-count*))
       (printf "%s failures, %s erros, %s pending\n"
         @*failure-count* @*error-count* @*pending-count*)
       (println))
   :no-tests
     (fn [ns-sym]
       (printf "No tests for %!" ns-sym))})