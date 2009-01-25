(ns cling.config
  (:use
    (clj-jdbc data-sources)
    (clj-log core)
    (weld config)
    (cling routing))
  (:require
    (weld app))
  (:import
    (java.io File)))

(def env (keyword (System/getProperty "cling.env")))

(def dev?  (= env :dev))
(def test? (= env :test))
(def prod? (= env :prod))

(def host "http://localhost:8080")

(def public (File. "public"))
(def statics '("/stylesheets" "/javascripts" "/favicon.ico"))

(def data-source
  (pg-data-source
    (cond
      prod? {:database "cling_prod" :user "mmcgrana" :password ""}
      dev?  {:database "cling_dev"  :user "mmcgrana" :password ""}
      test? {:database "cling_test" :user "mmcgrana" :password ""})))

(def log-levels {:prod :info :dev :debug :test :error})
(def logger (new-logger :err (log-levels env)))

(def backtracing? dev?)
(def handler (if prod? 'cling.controllers/internal-error))

(def reloading? dev?)
(def reloadables '(cling.models cling.markup cling.diff cling.views cling.controllers))

(def session-cookie-key :cling_session_id)
(def session-secret-key "sj746hdnzmdjd93746fhdknckdhd38019")

(def config
  {'weld.routing/*router*             router
   'weld.routing/*host*               host
   'weld.app/*logger*                 logger
   'weld.app/*handler-sym*            handler
   'weld.request/*session-cookie-key* session-cookie-key
   'weld.request/*session-secret-key* session-secret-key})

(use-config config)
