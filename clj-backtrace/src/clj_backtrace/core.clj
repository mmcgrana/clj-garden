(ns clj-backtrace.core
  (:use clj-backtrace.utils))

(defn- clojure-elem? [filename]
  "Returns true if the filename is non-null and indicates a clj source file."
  (and filename (re-matches? #"\.clj$" filename)))

(defn- clojure-ns [class-name]
  (re-get #"(([a-z-]+\.)+[^$]+)\$" class-name 1))

(defn- clojure-fn
  "Returns the clojure function name implied by the bytecode class name."
  [class-name]
  (re-get #"\$([a-z-]+)" class-name 1))

(defn clojure-annon-fn?
  "Returns true if the bytecode class name implies an annon fn."
  [class-name]
  (re-matches? #"\$fn__" class-name))

(defn parse-elem
  "Returns a map of information about the trace element."
  [elem]
  (let [class-name (.getClassName elem)
        file       (.getFileName  elem)
        line       (let [l (.getLineNumber elem)] (if (> l 0) l))
        parsed     {:file file :line line}]
    (if (clojure-elem? file)
      (assoc parsed
        :clojure true
        :ns       (clojure-ns class-name)
        :fn       (clojure-fn class-name)
        :annon-fn (clojure-annon-fn? class-name))
      (assoc parsed
        :java true
        :class class-name
        :method (.getMethodName elem)))))

(defn parse-trace
  "Returns a seq of maps providing usefull information about the stack
  trace elements."
  [elems]
  (map parse-elem elems))
