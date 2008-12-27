(use 'clj-unit.core)
(require '(clj-backtrace core-test repl-test))

(run-tests '(clj-backtrace.core-test clj-backtrace.repl-test))