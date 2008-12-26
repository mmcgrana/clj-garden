(ns cljurl.config)

(def *app-host* "localhost:8000")

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "cljurl_development")
    (.setUser         "mmcgrana")
    (.setPassword     "")))

(def +public-dir+
  (java.io.File. "public"))

(def +environment+ :dev)

(defn dev?  [] (= +environment+ :dev))
(defn test? [] (= +environment+ :test))

(defn show-exceptions? [] (or dev? test?))