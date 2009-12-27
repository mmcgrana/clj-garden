(use
  '(clojure.contrib repl-utils)
  '(clj-stacktrace repl))

(defn quit
  "Quit the clojure process with a 0 status code."
  []
  (System/exit 0))
