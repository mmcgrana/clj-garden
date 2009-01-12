(ns weld.request-test
  (:use clj-unit.core
        (weld request self-test-helpers)
        clojure.contrib.str-utils)
  (:require [clj-time.core :as time])
  (:load "request_cookies_test" "request_sessions_test"))

(deftest "headers"
  (assert= {"foo" "bar"} (headers (req-with {:headers {"foo" "bar"}}))))

(deftest "content-type"
  (assert=
    "application/xml"
    (content-type (req-with {:content-type "application/xml"}))))

(deftest "content-length"
  (assert= 37
    (content-length (req-with {:content-length 37}))))

(deftest "character-encoding"
  (assert= "UTF-8"
    (character-encoding (req-with {:character-encoding "UTF-8"}))))

(deftest "query-string"
  (assert= "foo=bar"
    (query-string (req-with {:query-string "foo=bar"}))))

(deftest "uri: before route params"
  (assert= "/foo/bar"
    (uri (req-with {:uri "/foo/bar"}))))

(deftest "query-params"
  (assert= {:foo {:bar "bat"}}
    (query-params (req-with {:query-string "foo[bar]=bat"}))))

(deftest "body-str"
  (assert= "foobar"
    (body-str (req-with {:body (str-input-stream "foobar")}))))

(deftest "form-params"
  (assert= nil (form-params (req-with {:body "foo=bar"})))
  (assert= {:foo {:bar "bat"}}
    (form-params (req-with {:body (str-input-stream "foo[bar]=bat")
                            :content-type "application/x-www-form-urlencoded"}))))

; TODO: multipart-params - needs pretty elaborate helper infrastructure

(deftest "mock-params"
  (assert= {:foo "bar"}
    (mock-params (req-with {:weld.request/mock-params {:foo "bar"}}))))

(deftest "params*"
  (binding [weld.request/multipart-params (constantly {:multipart "multipart"})]
    (assert= {:query "query" :multipart "multipart" :mock "mock"}
      (params* (req-with {:query-string "query=query"
                          :weld.request/mock-params {:mock "mock"}}))))
  (binding [weld.request/multipart-params (constantly nil)]
    (assert= {:query "query" :form "form" :mock "mock"}
      (params* (req-with {:query-string "query=query"
                          :body (str-input-stream "form=form")
                          :content-type "application/x-www-form-urlencoded"
                          :weld.request/mock-params {:mock "mock"}})))))

(deftest "params"
  (assert= {:query "query" :route "route"}
    (params (req-with {:query-string "query=query"
                       :weld.request/route-params {:route "route"}})))
  (assert= "bat"
    (let [the-env (req-with {:query-string "foo[bar]=bat"})]
      (params the-env :foo :bar))))

(deftest "request-method*"
  (assert= :get (request-method* (req-with {:request-method :get}))))

(deftest "request-method"
  (assert= :get  (request-method (req-with {:request-method :get})))
  (assert= :post (request-method (req-with {:request-method :post})))
  (assert= :delete
    (request-method (req-with {:request-method :post
                               :weld.request/mock-params {:_method "delete"}})))
  (assert-throws #"Unrecognized piggyback method :post"
    (request-method (req-with {:request-method :post
                               :weld.request/mock-params {:_method "bar"}})))
  (assert-throws #"Unrecognized :request-method :bar"
    (request-method (req-with {:request-method :bar}))))

(deftest "scheme"
  (assert= :http
    (scheme (req-with {:scheme :http}))))

(deftest "ssl?"
  (assert-not  (ssl? (req-with {:scheme "http"})))
  (assert-that (ssl? (req-with {:scheme "https"}))))

(deftest "server-port"
  (assert= 80 (server-port (req-with {:server-port 80}))))

; TODO: need to review the whole server host / name / port thing
(def xfhost-header {"x-forwarded-host" "google.com"})
(def h-header      {"host"             "yahoo.com"})
(def server-attrs  {:server-name "ask.com" :server-port 80})

(deftest "server-host"
  (assert= "google.com"
    (server-host (req-with (assoc server-attrs
                             :headers (merge xfhost-header h-header)))))
  (assert= "yahoo.com"
    (server-host (req-with (assoc server-attrs :headers h-header))))
  (assert= "ask.com"
    (server-host (req-with server-attrs))))

(deftest "full-uri"
  (assert= "https://google.com/foo/bar"
    (full-uri (req-with {:uri "/foo/bar" :scheme "https"
                         :headers {"host" "google.com"}}))))

(deftest "user-agent"
  (assert= "browser"
    (user-agent (req-with {:headers {"user-agent" "browser"}}))))

(deftest "ajax?"
  (assert-that
    (ajax? (req-with {:headers {"x-requested-with" "XMLHttpRequest"}})))
  (assert-not
    (ajax? (req-with {}))))

(deftest "remote-ip"
  (assert= "rem.addr.only"
    (remote-ip
      (req-with {:remote-addr "rem.addr.only"})))
  (assert= "www.example.com"
    (remote-ip
      (req-with {:headers {"x-forwarded-for" "www.example.com"}})))
  (assert= "www.example.com"
    (remote-ip
      (req-with {:headers {"x-forwarded-for" "192.168.2.1,127.0.0.1,www.example.com"}})))
  (assert= "the.client.ip"
    (remote-ip
      (req-with {:remote-addr "rem.addr.only"
                 :headers {"client-ip" "the.client.ip"
                           "x-forwarded-for" "www.example.com"}}))))

(deftest "referrer"
  (assert= "site"
    (referrer (req-with {:headers {"http-referer" "site"}}))))
