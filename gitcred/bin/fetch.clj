(ns fetch
  (:import  (java.io File))
  (:require (fleetdb [embedded :as embedded])
            (gitcred [data :as data])))

(if-let [path (first *command-line-args*)]
  (do
    (println "loading database")
    (let [dba (if (.exists (File. path))
                    (embedded/load-persistent path)
                    (embedded/init-persistent path))]
          (try
            (data/ensure-indexes dba)
            (data/fetch-graph-data dba)
            (finally
              (embedded/close dba)))))
  (do (println "Usage: clj bin/fetch.clj /path/to/data.fdb")
      (System/exit 1)))
