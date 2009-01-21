(ns weldblog.config
  (:use clj-jdbc.data-sources clj-log.core weld.config weldblog.routing)
  (:require weld.app)
  (:import java.io.File))

(def env (keyword (System/getProperty "weldblog.env")))

(def dev?  (= env :dev))
(def test? (= env :test))
(def prod? (= env :prod))

(def host "localhost:8000")

(def public (File. "public"))
(def statics '("/stylesheets" "/javascripts" "/favicon.ico"))

(def data-source
  (pg-data-source
    (cond
      prod? {:database "weldblog_prod" :user "mmcgrana" :password ""}
      dev?  {:database "weldblog_dev"  :user "mmcgrana" :password ""}
      test? {:database "weldblog_test" :user "mmcgrana" :password ""})))

(def log-levels {:prod :info :dev :debug :test :error})
(def logger (new-logger :err (log-levels env)))

(def exception-backtrace? dev?)
(def exception-handling?  prod?)
(def exception-logging?   (not test?))

(def reloading? dev?)
(def reloadables '(weldblog.models weldblog.views weldblog.controllers))

(def session-cookie-key :weldblog_session_id)
(def session-secret-key "sj746hdnzmdjd93746fhdknckdhd38019")

(def admin-password "secret")

(def config
  {'weld.routing/*router*             router
   'weld.app/*logger*                 logger
   'weld.request/*session-cookie-key* session-cookie-key
   'weld.request/*session-secret-key* session-secret-key})

(use-config config)
