(ns clj-backtrace.repl
  (:use (clj-backtrace core utils)))

(defn source-str [parsed]
  (if (and (:file parsed) (:line parsed))
    (str (:file parsed) ":" (:line parsed))
    "(Unknown Source)"))

(defn- clojure-method-str [parsed]
  (str (:ns parsed) "/" (:fn parsed) (if (:annon-fn parsed) "[fn]")))

(defn java-method-str [parsed]
  (str (:class parsed) "." (:method parsed)))

(defn- method-str [parsed]
  (if (:java parsed) (java-method-str parsed) (clojure-method-str parsed)))

(defn print-trace
  "Print a pretty stack trace for the elems."
  [elems]
  (let [parsed    (parse-trace elems)
        sources   (map source-str parsed)
        methods   (map method-str parsed)
        src-width (high (map (memfn length) sources))]
    (doseq [[src meth] (zip sources methods)]
      (println (str " " (rjust (+ src-width 3) src) " " meth)))))

(defn- ppe-cause
  "TODOC"
  [e]
  (println (str "Caused by: " e))
  (print-trace (.getStackTrace e))
  (if-let [cause (.getCause e)]
    (ppe-cause cause)))

(defn ppe
  "Print a pretty stack trace for *e."
  []
  (println (str *e))
  (print-trace (.getStackTrace *e))
  (if-let [cause (.getCause *e)]
    (ppe-cause cause)))
