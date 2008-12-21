(ns cljurl.config)

(def *app-host* "localhost:8000")

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "cljurl_development")
    (.setUser         "mmcgrana")
    (.setPassword     "")))