(ns calc
  (:require (fleetdb [embedded :as embedded])
            (gitcred [data :as data] [view :as view])))

(let [[data-path results-path] *command-line-args*]
  (if (and data-path results-path)
    (do (println "loading database")
      (let [dba     (embedded/load-ephemeral data-path)
            follows (data/follows-data dba)]
      (view/print-results follows results-path)))
    (do (println "Usage: clj bin/calc.clj /path/to/data.fdb /path/to/results.txt")
        (System/exit 1))))

