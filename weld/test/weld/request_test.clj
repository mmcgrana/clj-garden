(ns weld.request-test
  (:use clj-unit.core weld.request)
  (:require [clj-time.core :as time])
  (:import (java.io ByteArrayInputStream)))

(defn str-input-stream
  "Returns a ByteArrayInputStream for the given String."
  [string]
  (ByteArrayInputStream. (.getBytes string)))

(def base-env
  {:server-port        80
   :server-name        "localhost"
   :remote-addr        nil
   :uri                "/foo/bar"
   :query-string       ""
   :scheme             "http"
   :request-method     :get
   :headers            {}
   :content-type       nil
   :content-length     nil
   :character-encoding nil
   :body               (str-input-stream "")})

(defn env-with
  [attrs]
  (merge base-env attrs))

(deftest "headers"
  (assert= {"foo" "bar"} (headers (env-with {:headers {"foo" "bar"}}))))

(deftest "content-type"
  (assert=
    "application/xml"
    (content-type (env-with {:content-type "application/xml"}))))

(deftest "content-length"
  (assert= 37
    (content-length (env-with {:content-length 37}))))

(deftest "character-encoding"
  (assert= "UTF-8"
    (character-encoding (env-with {:character-encoding "UTF-8"}))))

(deftest "query-string"
  (assert= "foo=bar"
    (query-string (env-with {:query-string "foo=bar"}))))

(deftest "uri: before route params"
  (assert= "/foo/bar"
    (uri (env-with {:uri "/foo/bar"}))))

(deftest "query-params"
  (assert= {:foo {:bar "bat"}}
    (query-params (env-with {:query-string "foo[bar]=bat"}))))

; TODO: body-str
; TODO: form-params
; TODO: multipart-params
; TODO: mock-params
; TODO: params*
; TODO: params

(deftest "request-method*"
  (assert= :get (request-method* (env-with {:request-method :get}))))

(deftest "request-method"
  (assert= :get  (request-method (env-with {:request-method :get})))
  (assert= :post (request-method (env-with {:request-method :post})))
  ; TODO: when :post, test for piggyback in params
  ; TODO: when :post, test for invalid piggyback
  ; TODO: unrecognize method :foobar
  )

(deftest "scheme"
  (assert= "https"
    (scheme (env-with {:scheme "https"}))))

(deftest "ssl?"
  (assert-not  (ssl? (env-with {:scheme "http"})))
  (assert-that (ssl? (env-with {:scheme "https"}))))

(deftest "server-port"
  (assert= 80 (server-port (env-with {:server-port 80}))))

; Todo: need to review the whole server host / name / port thing
(def xfhost-header {"x-forwarded-host" "google.com"})
(def h-header      {"host"             "yahoo.com"})
(def server-attrs  {:server-name "ask.com" :server-port 80})

(deftest "server-host"
  (assert= "google.com"
    (server-host (env-with (assoc server-attrs
                             :headers (merge xfhost-header h-header)))))
  (assert= "yahoo.com"
    (server-host (env-with (assoc server-attrs :headers h-header))))
  (assert= "ask.com"
    (server-host (env-with server-attrs))))

(deftest "full-uri"
  (assert= "https://google.com/foo/bar"
    (full-uri (env-with {:uri "/foo/bar" :scheme "https"
                         :headers {"host" "google.com"}}))))

(deftest "user-agent"
  (assert= "browser"
    (user-agent (env-with {:headers {"user-agent" "browser"}}))))

(deftest "ajax?"
  (assert-that
    (ajax? (env-with {:headers {"x-requested-with" "XMLHttpRequest"}})))
  (assert-not
    (ajax? (env-with {}))))

(deftest "remote-ip"
  (assert= "rem.addr.only"
    (remote-ip
      (env-with {:remote-addr "rem.addr.only"})))
  (assert= "www.example.com"
    (remote-ip
      (env-with {:headers {"x-forwarded-for" "www.example.com"}})))
  (assert= "www.example.com"
    (remote-ip
      (env-with {:headers {"x-forwarded-for" "192.168.2.1,127.0.0.1,www.example.com"}})))
  (assert= "the.client.ip"
    (remote-ip
      (env-with {:remote-addr "rem.addr.only"
                 :headers {"client-ip" "the.client.ip"
                           "x-forwarded-for" "www.example.com"}}))))

(deftest "referrer"
  (assert= "site"
    (referrer (env-with {:headers {"http-referer" "site"}}))))
