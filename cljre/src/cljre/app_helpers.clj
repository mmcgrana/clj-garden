(ns cljre.app-helpers
  (:use clj-html.core ring.controller)
  (:require (org.danlarkin [json :as json])))

(defn respond-js [body]
  (respond body {:content-type "text/javascript"}))

(defn respond-json [data]
  (respond-js (json/encode-to-str data :indent 2)))

