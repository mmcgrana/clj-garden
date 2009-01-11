(ns weldblog.config
  (:use clj-jdbc.data-sources clj-log4j.core)
  (:import java.io.File))

(def env (keyword (System/getProperty "weldblog.env")))

(def dev?  (= env :dev))
(def test? (= env :test))
(def prod? (= env :prod))

(def app-host "localhost:8000")

(def public-dir (File. "public"))

(def data-source
  (pg-data-source
    (cond
      prod? {:database "weldblog_prod" :user "mmcgrana" :password ""}
      dev?  {:database "weldblog_dev"  :user "mmcgrana" :password ""}
      test? {:database "weldblog_test" :user "mmcgrana" :password ""})))

(def logger
  (logger4j :err (cond prod? :info dev? :debug test? :error)))

(def exception-details?  dev?)
(def exception-handling? prod?)
(def exception-logging?  (not test?))

(def reloading? dev?)
(def reloadables '(weldblog.models weldblog.views weldblog.controllers))

(def admin-password "secret")



