(ns clj-backtrace.core
  (:use clojure.contrib.str-utils)
  (:use clj-backtrace.utils))

(defn- clojure-elem?
  "Returns true if the filename is non-null and indicates a clj source file."
  [class-name file]
  (or (re-match? #"^user" class-name)
      (and file (re-match? #"\.clj$" file))))

(defn- clojure-ns
  "Returns the clojure namespace name implied by the bytecode class name."
  [class-name]
  (let [base-name (re-get #"([^$]+)\$" class-name 1)
        hyph-name (re-gsub #"_" "-" base-name)]
    hyph-name))

(defn- clojure-fn
  "Returns the clojure function name implied by the bytecode class name."
  [class-name]
  (let [base-name (re-without #"(^[^$]+\$)|(__\d+(\$[^$]+)*$)" class-name)
        punc-name (re-gsub #"_QMARK_" "?" base-name)
        punc-name (re-gsub #"_BANG_"  "!" punc-name)
        punc-name (re-gsub #"_PLUS_"  "+" punc-name)
        punc-name (re-gsub #"_GT_"    ">" punc-name)
        punc-name (re-gsub #"_LT_"    "<" punc-name)
        punc-name (re-gsub #"_EQ_"    "=" punc-name)
        punc-name (re-gsub #"_STAR_"  "*" punc-name)
        punc-name (re-gsub #"_SLASH_" "/" punc-name)
        hyph-name (re-gsub #"_"       "-" punc-name)]
    hyph-name))

(defn- clojure-annon-fn?
  "Returns true if the bytecode class name implies an annon fn."
  [class-name]
  (re-match? #"\$fn__" class-name))

(defn parse-elem
  "Returns a map of information about the trace element."
  [elem]
  (let [class-name (.getClassName elem)
        file       (.getFileName  elem)
        line       (let [l (.getLineNumber elem)] (if (> l 0) l))
        parsed     {:file file :line line}]
    (if (clojure-elem? class-name file)
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

(defn trim-redundant-elems
  "Returns the portion of causer-elems that is not duplicated in caused-elems."
  [causer-elems caused-elems]
  (loop [rcauser-elems (reverse causer-elems)
         rcaused-elems (reverse caused-elems)]
    (if-let [rcauser-bottom (first rcauser-elems)]
      (if (= rcauser-bottom (first rcaused-elems))
        (recur (rest rcauser-elems) (rest rcaused-elems))
        (reverse rcauser-elems)))))

(defn- parse-cause-exception
  "Like parse-exception, but for causing exceptions."
  [causer-e caused-elems]
  (let [trace-elems (parse-trace (.getStackTrace causer-e))
        base {:class         (class causer-e)
              :message       (.getMessage causer-e)
              :trace-elems   trace-elems
              :trimmed-elems (trim-redundant-elems trace-elems caused-elems)}]
    (if-let [cause (.getCause causer-e)]
      (assoc base :cause (parse-cause-exception cause trace-elems))
      base)))

(defn parse-exception
  "Returns a Clojure data structure providing usefull informaiton about the
  exception, its stack trace elements, and its causes."
  [e]
  (let [trace-elems (parse-trace (.getStackTrace e))
        base {:class       (class e)
              :message     (.getMessage e)
              :trace-elems trace-elems}]
    (if-let [cause (.getCause e)]
      (assoc base :cause (parse-cause-exception cause trace-elems))
      base)))