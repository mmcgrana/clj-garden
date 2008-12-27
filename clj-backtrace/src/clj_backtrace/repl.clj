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

(defn trim-redundant-elems
  [causer-elems caused-elems]
  (loop [rcauser-elems (reverse causer-elems)
         rcaused-elems (reverse caused-elems)]
    (if-let [rcauser-bottom (first rcauser-elems)]
      (if (= rcauser-bottom (first rcaused-elems))
        (recur (rest rcauser-elems) (rest rcaused-elems))
        (reverse rcauser-elems)))))

(defn- ppe-cause
  "Print a pretty stack trace for an exception in a causal chain."
  [e-causer e-caused]
  (println (str "Caused by: " e-causer))
  (print-trace (trim-redundant-elems (.getStackTrace e-causer)
                                     (.getStackTrace e-caused)))
  (if-let [next-cause (.getCause e-causer)]
    (ppe-cause next-cause e-causer)))

(defn ppe
  "Print a pretty stack trace for an exception, by default *e."
  [& [e]]
  (let [exc (or e *e)]
    (println (str exc))
    (print-trace (.getStackTrace exc))
    (if-let [cause (.getCause exc)]
      (ppe-cause cause exc))))
