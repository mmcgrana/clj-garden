(ns cljurl.config)

(def +app-host+ "localhost:8000")

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "cljurl_development")
    (.setUser         "mmcgrana")
    (.setPassword     "")))

(def +public-dir+
  (java.io.File. "public"))

(def +env+ :dev)

(defn dev?  [] (= +env+ :dev))
(defn test? [] (= +env+ :test))
(defn prod? [] (= +env+ :prod))

(def +handle-exceptions+ nil)

(defn handle-exceptions? []
  (if (nil? +handle-exceptions+)
    (prod?)
    +handle-exceptions+))

