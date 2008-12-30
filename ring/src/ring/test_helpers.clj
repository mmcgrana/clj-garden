(ns ring.test-helpers
  (:require [ring.http-utils    :as http-utils]
            [org.danlarkin.json :as json])
  (:use clj-unit.core clj-scrape.core))

(defn request
  "Returns the response of app to mock request build according to the method,
  path and options."
  [app [method path] & [options]]
  (app {:uri path
        :request-method method
        :query-string (if-let [params (get options :params)]
                        (http-utils/query-unparse params))
        :remote-addr  (get options :remote-addr)}))

(defn assert-status
  "TODOC"
  [expected-status actual-status]
  (assert-truth (= expected-status actual-status)
    (format "Expected status of %s, but got %s"
      expected-status actual-status)))

(defn assert-redirect
  "TODOC"
  [expected-path actual-response]
  (let [[status headers body] actual-response
         location             (get headers "Location")]
     (assert-truth (and (and (>= status 300) (< status 400))
                       (= expected-path location))
       (format "Expecting redirect status and Location of %s, but got %s and %s."
         expected-path status location))))

(defmacro assert-selector
  "TODOC"
  [expected-selector actual-body]
  `(let [actual-dom# (dom (java.io.StringReader. ~actual-body))]
     (assert-truth (xml1-> actual-dom# ~@(cons 'desc expected-selector))
       (format "Expecting body matching %s, but did not.",
         '~expected-selector))))

(defn assert-content-type
  "TODOC"
  [expected-type actual-headers]
  (let [actual-type (get actual-headers "Content-Type")]
    (assert-truth (= expected-type actual-type)
      (format "Expecting Content-Type %s, but got %s"
        expected-type actual-type))))

(defn assert-json
  "TODOC"
  [expected-data actual-body]
  (assert-truth (= expected-data (json/decode-from-str actual-body))
    (format "Expecting JSON parsing to %s, but got %s"
      (prn-str expected-data) (prn-str actual-body))))