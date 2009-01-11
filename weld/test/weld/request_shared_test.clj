(in-ns 'weld.request-test)

(defn str-input-stream
  "Returns a ByteArrayInputStream for the given String."
  [string]
  (java.io.ByteArrayInputStream. (.getBytes string)))

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