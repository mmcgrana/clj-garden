(ns clj-log.core
  (:use clojure.contrib.except))

(def level-ranks
  {:debug 0
   :info  1
   :warn  2
   :error 3
   :fatal 4})

(def outputs
  {:err System/err
   :out System/out})

(defn log?
  [level-rank message-level]
  (>= level-rank (or (level-ranks message-level)
                     (throwf "Unrecognized level %s" message-level))))

(defn new-logger
  "Returns a logger configured with the specified output location and
  log level. output should be one of :err or :out, level one of :debug, :info,
  :warn, :error, :fatal."
  [output level]
  (let [level-rank (or (level-ranks level)
                       (throwf "Unrecognized level %s" level))
        output     (or (outputs output)
                       (throwf "Unrecognized output %s" output))]
    {:test #(log? level-rank %)
     :log  #(.println output %)}))
