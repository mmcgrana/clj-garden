(ns clj-jdbc.data-sources)

(defn pg-data-source
  "Returns a DataSource instance suitible for use in Stash models.
  Options: :database, :user, :password."
  [opts]
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName (get opts :database))
    (.setUser         (get opts :user))
    (.setPassword     (get opts :password))))