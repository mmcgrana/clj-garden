(ns cljurl.config
  (:use clj-jdbc.data-sources cljurl.utils clj-log4j)
  (:import java.io.File))

(def env (keyword (System/getProperty "cljurl.env")))

(def dev?  (= env :dev))
(def test? (= env :test))
(def prod? (= env :prod))

(def app-host "localhost:8000")

(def public-dir (File. "public"))

(def data-source
  (pg-data-source
    (cond
      prod? {:database "cljurl_prod" :user "deploy"   :password "somepass"}
      dev?  {:database "cljurl_dev"  :user "mmcgrana" :password ""}
      test? {:database "cljurl_test" :user "mmcgrana" :password ""})))

(def logger
  (logger4j :err (cond prod? :info dev? :debug test? :warn)))

(def show-exceptions?   dev?)
(def handle-exceptions? prod?)
(def log-exceptions?    (not test?))

(def reloading? dev?)
(def reloadable-ns-syms '(cljurl.controllers cljurl.models cljurl.views))



