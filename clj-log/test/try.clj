(ns clj-logger.try
  (:use clj-log.core))

(let [logger (new-logger :out :info)]
  (doseq [level '(:debug :info :warn :error :fatal)]
    (prn "trying" level)
    (prn ((:test logger) level))
    ((:log logger) (pr-str level))
    (prn)))
