(ns ring.request-test
  (:use clj-unit.core ring.request)
  (:import (java.io ByteArrayInputStream)))

;      req                     (from-env env)
;     [action-fn route-params] (recognizer (request-method req) (uri req))
;     request                  (assoc-route-params req route-params)]

(defn str-input-stream
  [string]
  (ByteArrayInputStream. (.getBytes string)))

(def env
  {:uri                "/foo/bar"
   :query-string       ""
   :scheme             "http"
   :request-method     :get
   :content-type       nil
   :content-length     nil
   :character-encoding nil
   :server-port        80
   :server-name        "localhost"
   :remote-addr        nil
   :headers            {}
   :stream-fn          (str-input-stream "")})

(defn req-with
  [attrs]
  (from-env (merge env attrs)))

(deftest "request-method: before route params"
  (assert= :get  (request-method (req-with {:request-method :get})))
  (assert= :post (request-method (req-with {:request-method :post})))
  ; TODO: test for body-params
  ; TODO: test for multipart params
  ; TODO: edge cases?
  )

(deftest "uri: before route params"
  (assert= "/foo/bar"
    (uri (req-with {:uri "/foo/bar"}))))

(defn request-with
  [attrs]
  (assoc-route-params
    (req-with attrs)
    {:slug "perm"}))

(deftest "content-type"
  (assert=
    "application/xml"
    (content-type (request-with {:content-type "application/xml"}))))

(deftest "content-length"
  (assert= 37
    (content-length (request-with {:content-length 37}))))

(deftest "character-encoding"
  (assert= "UTF-8"
    (character-encoding (request-with {:character-encoding "UTF-8"}))))

(deftest "query-string"
  (assert= "foo=bar"
    (query-string (request-with {:query-string "foo=bar"}))))

(deftest "query-params"
  (assert= {:foo {:bar "bat"}}
    (query-params (request-with {:query-string "foo[bar]=bat"}))))

; TODO: body-params

; TODO: multipart-params

; TODO: all parmas

(deftest "scheme"
  (assert= "https"
    (scheme (request-with {:scheme "https"}))))

(deftest "ssl?"
  (assert-not   (ssl? (request-with {:scheme "http"})))
  (assert-truth (ssl? (request-with {:scheme "https"}))))

(deftest "server-port"
  (assert= 80
    (server-port (request-with {:server-port 80}))))

(def xfhost-header {"x-forwarded-host" "google.com:80"})
(def h-header      {"host"             "yahoo.com:81"})
(def server-attrs  {:server-name "ask.com" :server-port 82})

; TODO: port issue
(deftest "server-host"
  (assert= "google.com:80"
    (server-host (request-with (assoc server-attrs
                                 :headers (merge xfhost-header h-header)))))
  (assert= "yahoo.com:81"
    (server-host (request-with (assoc server-attrs :headers h-header))))
  (assert= "ask.com:82"
    (server-host (request-with server-attrs))))

; TODO: does the port really belong here?
(deftest "full-uri"
  (assert= "https://google.com:80/foo/bar"
    (full-uri (request-with {:uri "/foo/bar" :scheme "https"
                             :headers {"host" "google.com:80"}}))))

; TODO: domain

(deftest "user-agent"
  (assert= "browser"
    (user-agent (request-with {:headers {"user-agent" "browser"}}))))

(deftest "ajax?"
  (assert-truth
    (ajax? (request-with {:headers {"x-requested-with" "XMLHttpRequest"}})))
  (assert-not
    (ajax? (request-with {}))))

; TODO: remote-ip

(deftest "referrer"
  (assert= "site"
    (referrer (request-with {:headers {"http-referer" "site"}}))))

; TODO: ability to access cached raw body

