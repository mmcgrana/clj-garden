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
  (init (merge base-env attrs)))

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

(deftest "body-str"
  (assert= "foobar"
    (body-str (env-with {:body (str-input-stream "foobar")}))))

(deftest "form-params"
  (assert= nil (form-params (env-with {:body "foo=bar"})))
  (assert= {:foo {:bar "bat"}}
    (form-params (env-with {:body (str-input-stream "foo[bar]=bat")
                            :content-type "application/x-www-form-urlencoded"}))))

; TODO: multipart-params - needs pretty elaborate helper infrastructure

(deftest "mock-params"
  (assert= {:foo "bar"}
    (mock-params (env-with {:weld.request/mock-params {:foo "bar"}}))))

(deftest "params*"
  (binding [weld.request/multipart-params (constantly {:multipart "multipart"})]
    (assert= {:query "query" :multipart "multipart" :mock "mock"}
      (params* (env-with {:query-string "query=query"
                          :weld.request/mock-params {:mock "mock"}}))))
  (binding [weld.request/multipart-params (constantly nil)]
    (assert= {:query "query" :form "form" :mock "mock"}
      (params* (env-with {:query-string "query=query"
                          :body (str-input-stream "form=form")
                          :content-type "application/x-www-form-urlencoded"
                          :weld.request/mock-params {:mock "mock"}})))))

(deftest "params"
  (assert= {:query "query" :route "route"}
    (params (env-with {:query-string "query=query"
                       :weld.request/route-params {:route "route"}})))
  (assert= "bat"
    (let [the-env (env-with {:query-string "foo[bar]=bat"})]
      (params the-env :foo :bar))))

(deftest "request-method*"
  (assert= :get (request-method* (env-with {:request-method :get}))))

(deftest "request-method"
  (assert= :get  (request-method (env-with {:request-method :get})))
  (assert= :post (request-method (env-with {:request-method :post})))
  (assert= :delete
    (request-method (env-with {:request-method :post
                               :weld.request/mock-params {:_method "delete"}})))
  (assert-throws #"Unrecognized piggyback method :post"
    (request-method (env-with {:request-method :post
                               :weld.request/mock-params {:_method "bar"}})))
  (assert-throws #"Unrecognized :request-method :bar"
    (request-method (env-with {:request-method :bar}))))

(deftest "scheme"
  (assert= "https"
    (scheme (env-with {:scheme "https"}))))

(deftest "ssl?"
  (assert-not  (ssl? (env-with {:scheme "http"})))
  (assert-that (ssl? (env-with {:scheme "https"}))))

(deftest "server-port"
  (assert= 80 (server-port (env-with {:server-port 80}))))

; TODO: need to review the whole server host / name / port thing
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
