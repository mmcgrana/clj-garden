(ns ringblog.config
  (:use clj-jdbc.data-sources)
  (:import java.io.File))

(def env (keyword (System/getProperty "ringblog.env")))

(def dev?  (= env :dev))
(def test? (= env :test))
(def prod? (= env :prod))

(def app-host "localhost:8000")

(def public-dir (File. "public"))

(def data-source
  (pg-data-source
    (cond
      prod? {:database "ringblog_prod" :user "mmcgrana" :password ""}
      dev?  {:database "ringblog_dev"  :user "mmcgrana" :password ""}
      test? {:database "ringblog_test" :user "mmcgrana" :password ""})))

(def logger
  (logger4j-err (cond prod? :info dev? :debug test? :error)))

(def exception-details?  dev?)
(def exception-handling? prod?)
(def exception-logging?  (not test?))

(def reloading? dev?)
(def reloadables '(ringblog models views controllers routing))



