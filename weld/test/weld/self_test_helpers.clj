(ns weld.self-test-helpers
  (:require weld.request)
  (:import java.io.ByteArrayInputStream))

(defn str-input-stream
  "Returns a ByteArrayInputStream for the given String."
  [string]
  (ByteArrayInputStream. (.getBytes string)))

(def base-req
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

(defn req-with
  [attrs]
  (weld.request/init (merge base-req attrs)))