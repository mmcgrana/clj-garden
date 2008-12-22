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

(defn ppe
  "Print a pretty stack trace for *e."
  []
  (let [parsed    (parse-trace (.getStackTrace *e))
        sources   (map source-str parsed)
        methods   (map method-str parsed)
        src-width (high (map (memfn length) sources))]
    (println (str *e))
    (doseq [[src meth] (zip sources methods)]
      (println (str " " (rjust src-width src) " " meth)))))
