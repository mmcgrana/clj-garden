;; Announce
(println "REPL Extensions")

;; Always-used libs
(use
  '(clojure.contrib repl-utils repl-ln duck-streams)
  'clj-backtrace.repl)

;; Macroexpansion utils
(defn- mapr
  "recursively maps a function to a sequence and
  subsequences within the map results."
  [f & sequence]
  (defn maprec [form]
    (if (seq? form)
      (map maprec (f form))
      form))
  (first (maprec sequence)))

(defn macroexpand-r
  "Expands all macros in a form and its subforms."
  [forms]
  (mapr macroexpand forms))

(defn macroexpand-only
  "Expands all macros in a form and its subforms that match a given symbol."
  [macro forms]
  (mapr
    (fn [f] (if (= macro (first f)) (macroexpand f) f))
    forms))

;; General REPL functions
(defn quit
  "Quit the clojure process with a 0 status code."
  []
  (System/exit 0))

(defn r
  "Enter the line-numbering repl"
  []
  (repl))

