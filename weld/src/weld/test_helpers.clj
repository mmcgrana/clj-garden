(ns weld.test-helpers
  (:require [weld.http-utils     :as http-utils]
            [clj-file-utils.core :as file-utils]
            [org.danlarkin.json  :as json])
  (:use clj-unit.core clj-scrape.core))

(defn request
  "Returns the response of app to mock request build according to the method,
  path and options."
  [app [method path] & [options]]
  (app {:uri            path
        :request-method method
        :mock-params    (get options :params)
        :remote-addr    (get options :remote-addr)}))

(defn upload
  "Returns an upload hash that can be used as a value in the :params map for
  the mock request helper."
  [file content-type filename]
  {:tempfile file     :size (file-utils/size file)
   :filename filename :content-type content-type})

(defn assert-status
  "Assert that a response status equals an expected status."
  [expected-status actual-status]
  (assert-truth (= expected-status actual-status)
    (format "Expected status of %s, but got %s"
      expected-status actual-status)))

(defn assert-redirect
  "Assert that a response tuple indicates a redirect to an expected path."
  [expected-path actual-response]
  (let [{:keys [status headers body]} actual-response
         location             (get headers "Location")]
     (assert-truth (and (and (>= status 300) (< status 400))
                       (= expected-path location))
       (format "Expecting redirect status and Location of %s, but got %s and %s."
         expected-path status location))))

(defn assert-markup
  "Assert that a response body matches an expected selector tuple."
  [expected-selector-tuple actual-body]
  (let [actual-dom (dom (java.io.StringReader. actual-body))]
    (assert-truth (apply xml1-> actual-dom :desc expected-selector-tuple)
      (format "Expecting body matching %s, but did not."
        expected-selector-tuple))))

(defn assert-content-type
  "Assert that response headers specify an expected content type."
  [expected-type actual-headers]
  (let [actual-type (get actual-headers "Content-Type")]
    (assert-truth (= expected-type actual-type)
      (format "Expecting Content-Type %s, but got %s"
        expected-type actual-type))))

(defn assert-json
  "Assert that the json encoded in a response body corresponds to a given
  hash/array data strucutre."
  [expected-data actual-body]
  (let [actual-data (json/decode-from-str actual-body)]
    (assert-truth (= expected-data actual-data)
      (format "Expecting JSON parsing to %s, but got %s"
        (prn-str expected-data) (prn-str actual-data)))))
