(ns weld.request
  (:use clojure.contrib.def
        clojure.contrib.str-utils
        clojure.contrib.except
        weld.http-utils
        weld.utils)
  (:require [clj-time.core :as time])
  (:import (org.apache.commons.fileupload FileUpload RequestContext)
           (org.apache.commons.fileupload.disk DiskFileItemFactory DiskFileItem)
           (org.apache.commons.io IOUtils)
           (java.io InputStream)
           (org.joda.time.format DateTimeFormat))
  (:load "request_cookies" "request_sessions"))

(defvar- multipart-re         #"multipart/form-data")
(defvar- form-url-encoded-re  #"^application/x-www-form-urlencoded")
(defvar- ajax-http-request-re #"(?i)XMLHttpRequest")
(defvar- local-ip-re          #"^(?i)unknown$|^(127|10|172.16|192.168)\.")

(defvar- recognized-nonpiggyback-methods
  #{:get :head :put :delete :options}
  "A set of Keywords corresponding to recognized nonpiggyback http methods.")

(defn headers
  "Returns the raw headers for the request."
  [req]
  (:headers req))

(defn content-type
  "Returns a String for the requests's content type."
  [req]
  (:content-type req))

(defn content-length
  "Returns an Integer for the requests's content length."
  [req]
  (:content-length req))

(defn character-encoding
  "Returns a String for the request's character encoding."
  [req]
  (:character-encoding req))

(defn query-string
  "Returns a String for the request's query string."
  [req]
  (:query-string req))

(defn uri
  "Returns a String for the requests's uri."
  [req]
  (:uri req))

(defn query-params
  "Returns a possible nested map of query params based on the request's query
  string, or nil if there was no query string."
  [req]
  (query-parse (query-string req)))

(defn body-str
  "Returns a single String of the raw request body. Should  be called only 
  within an exclusive guard that rules out all other such guards, i.e. it must
  be called no more than once. Should not be called if the reader input is to 
  be used."
  [req]
  (force (:weld.request/body-str-delay req)))

(defn- body-str-io
  [req]
  (with-open [#^InputStream stream (:body req)]
    (IOUtils/toString stream)))

(defn form-params
  "Returs a hash of params described by the request's post body if the 
  content-type indicates a form url encoded request, nil otherwise."
  [req]
  (if-let [ctype (content-type req)]
    (if (re-match? form-url-encoded-re ctype)
      (query-parse (body-str req)))))

(def #^{:private true
        :doc "Multipart parsing handler. Saves all multipart param values
              as tempfiles, regardless of size."}
  disk-file-item-factory
  (doto (DiskFileItemFactory.)
    (.setSizeThreshold -1)
    (.setFileCleaningTracker nil)))

(defn multipart-params
  "Returns a hash of multipart params if the content-type indicates a multipart
  request, nil otherwise."
  [req]
  (force (:weld.request/multipart-params-delay req)))

(defn- multipart-params-io
  [req]
  (if-let [ctype (content-type req)]
    (if (re-match? multipart-re ctype)
      (let [upload  (FileUpload. disk-file-item-factory)
            context (proxy [RequestContext] []
                      (getContentType       [] (content-type req))
                      (getContentLength     [] (content-length req))
                      (getCharacterEncoding [] (character-encoding req))
                      (getInputStream       [] (:body req)))
            items   (.parseRequest upload context)
            pairs   (map
                      (fn [#^DiskFileItem item]
                        [(.getFieldName item)
                         (if (.isFormField item)
                           (.getString item)
                           ; need first pair to prevent premature tempfile GC
                           {:disk-file-item item
                            :filename       (.getName item)
                            :size           (.getSize item)
                            :content-type   (.getContentType item)
                            :tempfile       (.getStoreLocation item)})])
                      items)]
      (pairs-parse pairs)))))

(defn mock-params
  "Returns a hash of mock params given directly in the req, if any.
  Used for testing."
  [req]
  (:weld.request/mock-params req))

(def #^{:doc "Returns all params except for those determined from the route."}
  params*
  (memoize-by :weld.request/memoization-key
    (fn [req]
      (merge (query-params     req)
             (form-params      req)
             (multipart-params req)
             (mock-params      req)))))

(defn params
  "Returns params, including those determined from the route.
  If a single arg is given, an req, returns all such params.
  If additional args are given, they are used to get-in these params"
  ([req]
     (merge (params* req) (:weld.request/route-params req)))
  ([req & args]
   (get-in (params req) args)))

(defn request-method*
  "Returns the literal request method indicated in the req, before taking
  into account piggybacking."
  [req]
  (:request-method req))

(defn request-method
  "Returns a Keyword indicating the method for the request, either as literally
  indicated by the request or if available by the _method piggyback param.
  Throws if no valid method is recognized."
  [req]
  (let [r-method (request-method* req)]
    (cond
      (recognized-nonpiggyback-methods r-method)
        r-method
      (= :post r-method)
        (if-let [p-method (:_method (params* req))]
          (or (recognized-nonpiggyback-methods (keyword p-method))
              (throwf "Unrecognized piggyback method %s" r-method))
          :post)
      :else
        (throwf "Unrecognized :request-method %s" r-method))))

(defn scheme
  "Returns a Keyword inidcating the scheme for the request, like :http."
  [req]
  (:scheme req))

(defn ssl?
  "Returns tree iff the request was submitted made over ssl."
  [req]
  (= "https" (scheme req)))

(defn server-port
  "Returns a Integer for the server port."
  [req]
  (:server-port req))

(defn server-host
  "Returns a String for the full hostname, excluding the port."
  [req]
  (let [hdrs (headers req)]
    (or (get hdrs "x-forwarded-host")
        (get hdrs "host")
        (:server-name req))))

(defn full-uri
  "Returns a String for the full request uri, including the protocol and host
  but excluding the port."
  [req]
  (str (scheme req) "://" (server-host req) (uri req)))

(defn user-agent
  "Returns a String for the http user agent."
  [req]
  (get-in req [:headers "user-agent"]))

(defn ajax?
  "Returns true if the given request was an AJAX request."
  [req]
  (if-let [xrw (get-in req [:headers "x-requested-with"])]
    (re-match? ajax-http-request-re xrw)))

(defn remote-ip
  "Returns a String representing our best guess for the IP of the requesting 
  user."
  [req]
  (let [headers (:headers req)]
    (or (get headers "client-ip")
        (if-let [forwarded (get headers "x-forwarded-for")]
          (let [all-ips       (re-split #"," forwarded)
                remote-ips (remove #(re-match? local-ip-re %) all-ips)]
            (if (not (empty? remote-ips)) (trim (first remote-ips)))))
        (:remote-addr req))))

(defn referrer
  "Returns a String for the http refer(r)er"
  [req]
  (get-in req [:headers "http-referer"]))

(defn init
  "Returns a prepared request based on the given raw reqest, where the
  preparations enable the request to correctly handle memoization."
  [req]
  (assoc req
    :weld.request/memoization-key        (clojure.lang.RT/nextID)
    :weld.request/multipart-params-delay (delay (multipart-params-io req))
    :weld.request/body-str-delay         (delay (body-str-io req))))

(defn assoc-route-params
  "Returns a new request object like the corresponding to the given one but 
  including the given route-params."
  [req route-params]
  (assoc req :weld.request/route-params route-params))