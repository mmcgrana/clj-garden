(ns cljurl.app.controller_helpers
  (:use ring.controller))

(defn respond-json
  "Render a JSON response with the body."
  [body & [opts]]
  (respond body (assoc opts :content-type "text/javascript")))

(defn respond-json-404
  "Render a JSON response with the body and a 404 status."
  [body]
  (respond-json body {:status 404}))

(defn respond-json-500
  "Render a JSONE response with the body and a 500 status."
  [body]
  (respond-json body {:status 500}))