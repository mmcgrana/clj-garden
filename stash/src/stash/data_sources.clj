(ns stash.data-sources)

(defn pg-data-source
  [opts]
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName (get opts :database))
    (.setUser         (get opts :user))
    (.setPassword     (get opts :password))))