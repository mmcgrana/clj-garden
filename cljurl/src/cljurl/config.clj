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

(def +show-exceptions+ nil)
(def +handle-exceptions+ nil)

(defn show-exceptions? []
  (let [force +show-exceptions+]
    (cond
      (= force true)  true
      (= force false) false
      :else           (dev?))))

(defn handle-exceptions? []
  (let [handle +handle-exceptions+]
    (cond
      (= handle true)  true
      (= handle false) false
      :else            (prod?))))

