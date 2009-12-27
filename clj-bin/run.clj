(use 'clj-stacktrace.repl)
(import 'clojure.lang.Compiler)

(let [script-path (first *command-line-args*)
      script-args (rest  *command-line-args*)]
  (binding [*command-line-args* script-args]
    (try
      (Compiler/loadFile script-path)
      (catch Exception e
        (pst-on *err* true e)
        (System/exit 1)))))
