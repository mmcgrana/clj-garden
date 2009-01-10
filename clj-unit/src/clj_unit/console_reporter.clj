(ns clj-unit.console-reporter
  (:use clj-unit.utils clojure.contrib.str-utils
        (clj-backtrace core repl)))

(defn- test-runner-elem?
  [elem]
  (and (= (:ns elem) "clj-unit.core")
       (= (:fn elem) "run-tests")))

(defn print-exception
  "A sort of modified .printStackTrace. Prints to *out* as apposed to *err* and
  only prints those stack elements above the test invocation code."
  [#^Exception e]
  (let [parsed (parse-exception e)
        elems  (:trace-elems parsed)
        user-elems (take-while (complement test-runner-elem?) elems)]
    (binding [*use-color* true]
      (println (with-color :red (str e)))
      (print-trace-elems user-elems))))

(def +console-reporter+
  {:init
    (fn [ns-syms]
      (println)
      (println "Testing:" (str-join ", " (map str ns-syms)))
      {:test-count    0
       :success-count 0
       :pass-count    0
       :failure-count 0
       :error-count   0
       :pending-count 0
       :start-time    (System/currentTimeMillis)})
   :start
     (fn [state ns-sym] state)
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
       (printf "\nFAIL: %s (%s:%s)\n%s\n"
         (:doc test-info) (:file test-info) (:line test-info) message)
       (update state :failure-count inc))
   :error
     (fn [state test-info #^Exception e]
       (printf "\nEXCP: %s (%s:%s)\n"
         (:doc test-info) (:file test-info) (:line test-info))
       (print-exception e)
       (update state :error-count inc))
   :finish
     (fn [state ns-sym] state)
   :end
     (fn [state]
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