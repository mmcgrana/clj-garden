(ns 'clj-log4j.core
  (:use (clojure.contrib except case))
  (:import (org.apache.log4j Level Logger ConsoleAppender SimpleLayout)))

(def log-levels
  {:debug Level/DEBUG
   :info  Level/INFO
   :warn  Level/WARN
   :error Level/ERROR
   :fatal Level/FATAL})

(defn log-level
  "Returns a static field of Level corresponding to the lower case keyword
  level."
  [level]
  (or (log-levels level) (throwf "unrecognized log level: %s" level)))

(defn logger4j
  "Returns a console log4j logger with the specified log level, which should
  be a keyword like :info. Output can be one of :err or :out."
  [output level]
  (let [apdr (doto (ConsoleAppender.)
               (.setTarget
                 (case output
                   :out (ConsoleAppender/SYSTEM_OUT)
                   :err (ConsoleAppender/SYSTEM_ERR)))
               (.setLayout (SimpleLayout.))
               (.activateOptions))]
    (doto (Logger/getLogger (str (gensym)))
      (.setLevel (log-level level))
      (.addAppender apdr))))
