(ns ring.request
  (:use clojure.contrib.def
        clojure.contrib.str-utils
        clojure.contrib.except
        ring.http-utils
        ring.utils)
  (:import (org.apache.commons.fileupload FileUpload RequestContext)
           (org.apache.commons.fileupload.disk DiskFileItemFactory DiskFileItem)
           (org.apache.commons.io IOUtils)
           (java.io InputStream)))

(defvar- +multipart-re+         #"multipart/form-data")
(defvar- +form-url-encoded-re+  #"^application/x-www-form-urlencoded")
(defvar- +ajax-http-request-re+ #"(?i)XMLHttpRequest")
(defvar- +local-ip-re+          #"^(?i)unknown$|^(127|10|172.16|192.168)\.")

(defvar- +recognized-nonpiggyback-methods+
  #{:get :head :put :delete :options}
  "A set of Keywords corresponding to recognized nonpiggyback http methods.")

(defn content-type
  "Returns a String for the request's content type."
  [request]
  ((request :env) :content-type))

(defn content-length
  "Returns an Integer for the request's content length."
  [request]
  ((request :env) :content-length))

(defn character-encoding
  "Returns a String for the request's character encoding."
  [request]
  ((request :env) :character-encoding))

(defn query-string
  "Returns a String for the request's query string."
  [request]
  ((request :env) :query-string))

(defn uri
  "Returns a String for the requests's uri."
  [request]
  ((request :env) :uri))

(defn query-params
  "Returns a possible nested map of query params based on the request's query
  string, or nil if there was no query string."
  [request]
  (request :query-params))

(defn form-params
  "Returs a hash of params described by the request's post body if the 
  content-type indicates a form url encoded request, nil otherwise."
  [request]
  (request :form-params))

(defn multipart-params
  "Returns a hash of multipart params if the content-type indicates a multipart
  request, nil otherwise."
  [request]
  (request :multipart-params))

(defn mock-params
  "Returns a hash of mock params given directly in the env, if any."
  [request]
  (request :mock-params))

(defn params
  "If only the request is given, returns the merged map of all params from the
  request. If additional args are given, they are treated as keys with which to
  get-in from the params".
  ([request] (request :params))
  ([request & keys] (get-in (params request) keys)))

(defn request-method
  "Returns a Keyword indicating the method for the request, either as literally
  indicated by the request or if available by the _method piggyback param.
  Throws if no valid method is recognized."
  [request]
  (request :request-method))

(defn scheme
  "Returns a String inidcating the scheme for the request, like http or https."
  [request]
  ((request :env) :scheme))

(defn ssl?
  "Returns tree iff the request was submitted made over ssl."
  [request]
  (= "https" (scheme request)))

(defn server-port
  "Returns a Integer for the server port."
  [request]
  ((request :env) :server-port))

(defn server-host
  "Returns a String for the full hostname, including the port."
  [request]
  (let [env     (request :env)
        headers (env :headers)]
    (or (get headers "x-forwarded-host")
        (get headers "host")
        (str (env :server-name) ":" (env :server-port)))))

(defn full-uri
  "Returns a String for the full request uri, including the protocol and host."
  [request]
  (str (scheme request) "://" (server-host request) (uri request)))

(defn subdomains
  "Returns a seq Strings for the n topmost domains in the host, where n defaults 
  to 1 if it is not specified."
  ([request]
    (subdomains request 1))
  ([request n-toplevels]
    (take-last n-toplevels (re-split #"." (server-host request)))))

; TODO: no need to add and remove the port here
(defn domain
  "Returns a String for the full domain without port number name as constitued 
  by the n topmost domains in the host, where n defaults to 1."
  ([request]
    (domain request 1))
  ([request n-toplevels]
    (re-without #":\d+$"
      (str-join "."
        (take-last (inc n-toplevels) (re-split #"." (server-host request)))))))

(defn user-agent
  "Returns a String for the http user agent."
  [request]
  (((request :env) :headers) "user-agent"))

(defn ajax?
  "Returns true if the given request was an AJAX request."
  [request]
  (if-let [xrw (((request :env) :headers) "x-requested-with")]
    (re-match? +ajax-http-request-re+ xrw)))

(defn remote-ip
  "Returns a String representing our best guess for the IP of the requesting 
  user."
  [request]
  (let [env     (request :env)
        headers (env :headers)]
    (or (get headers "client-ip")
        (if-let [forwarded (get headers "x-forwarded-for")]
          (let [all-ips       (re-split #"," forwarded)
                remote-ips (remove #(re-match? +local-ip-re+ %) all-ips)]
            (if (not (empty? remote-ips)) (.trim #^String (first remote-ips)))))
        (env :remote-addr))))

(defn referrer
  "Returns a String for the http refer(r)er"
  [request]
  (((request :env) :headers) "http-referer"))


(defn- raw-body
  "Returns a single String of the raw request body. Should  be called only 
  within an exclusive guard that rules out all other such guards, i.e. it must
  be called no more than once. Should not be called if the reader input is to 
  be used."
  [request]
  (with-open [#^InputStream stream (((request :env) :stream-fn))]
    (IOUtils/toString stream)))

(defn- parse-form-params
  "When the request is form-url-encoded, returns the params Map parsed from
  the raw-body, otherwise returns nil."
  [request]
  (if-let [ctype (content-type request)]
    (if (re-match? +form-url-encoded-re+ ctype)
      (query-parse (raw-body request)))))

(defn- parse-multipart-params
  "When the requst is multipart-encoded, returns the params Map parsed from
  the multipart body, otherwise returns nil."
  [request]
  (if-let [ctype (content-type request)]
    (if (re-match? +multipart-re+ ctype)
      (let [factory (doto (DiskFileItemFactory.) (.setSizeThreshold -1))
            upload  (FileUpload. factory)
            context (proxy [RequestContext] []
                      (getContentType       [] (content-type request))
                      (getContentLength     [] (content-length request))
                      (getCharacterEncoding [] (character-encoding request))
                      (getInputStream       [] (((request :env) :stream-fn))))
            items   (.parseRequest upload context)
            pairs   (map
                      (fn [#^DiskFileItem item]
                        [(.getFieldName item)
                         (if (.isFormField item)
                           (.getString item)
                           {:filename     (.getName item)
                            :size         (.getSize item)
                            :content-type (.getContentType item)
                            :tempfile     (.getStoreLocation item)})])
                      items)]
      (pairs-parse pairs)))))

(defn- determine-method
  "Returns the keyword indicating the http method for the quest, as indicated
  by the literal request method or by the _method param."
  [request]
  (let [request-method ((request :env) :request-method)]
    (cond
      (+recognized-nonpiggyback-methods+ request-method)
        request-method
      (= :post request-method)
        (if-let [given-method (params request :_method)]
          (or (+recognized-nonpiggyback-methods+ (keyword given-method)) :post)
          :post)
      :else
        (throwf "Unrecognized :request-method %s" request-method))))

(defn from-env
  "Returns a partial request object based on the given env. A complete request
  object is obtained via assoc-route-params."
  [env]
  (let [request          {:env env}
        request          (assoc request :query-params
                           (query-parse (query-string request)))
        request          (assoc request :form-params
                           (parse-form-params request))
        request          (assoc request :multipart-params
                           (parse-multipart-params request))
        request          (assoc request :mock-params
                           (:mock-params env))
        request          (assoc request :params
                           (merge (query-params request)
                                  (form-params request)
                                  (multipart-params request)
                                  (mock-params request)))
        request          (assoc request :request-method
                           (determine-method request))]
  request))

(defn assoc-route-params
  "Returns a new request object like the given one but including the given
  route-params"
  [request route-params]
  (assoc request
    :route-params route-params
    :params       (merge (request :params) route-params)))