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
           (org.joda.time.format DateTimeFormat)))

(defvar- multipart-re         #"multipart/form-data")
(defvar- form-url-encoded-re  #"^application/x-www-form-urlencoded")
(defvar- ajax-http-request-re #"(?i)XMLHttpRequest")
(defvar- local-ip-re          #"^(?i)unknown$|^(127|10|172.16|192.168)\.")

(defvar- recognized-nonpiggyback-methods
  #{:get :head :put :delete :options}
  "A set of Keywords corresponding to recognized nonpiggyback http methods.")

(defn headers
  "Returns the raw headers for the request."
  [env]
  (:headers env))

(defn content-type
  "Returns a String for the requests's content type."
  [env]
  (:content-type env))

(defn content-length
  "Returns an Integer for the requests's content length."
  [env]
  (:content-length env))

(defn character-encoding
  "Returns a String for the request's character encoding."
  [env]
  (:character-encoding env))

(defn query-string
  "Returns a String for the request's query string."
  [env]
  (:query-string env))

(defn uri
  "Returns a String for the requests's uri."
  [env]
  (:uri env))

(defn query-params
  "Returns a possible nested map of query params based on the request's query
  string, or nil if there was no query string."
  [env]
  (query-parse (query-string env)))

(defn- body-str
  "Returns a single String of the raw request body. Should  be called only 
  within an exclusive guard that rules out all other such guards, i.e. it must
  be called no more than once. Should not be called if the reader input is to 
  be used."
  [env]
  (with-open [#^InputStream stream (:body env)]
    (IOUtils/toString stream)))

(defn form-params
  "Returs a hash of params described by the request's post body if the 
  content-type indicates a form url encoded request, nil otherwise."
  [env]
  (if-let [ctype (content-type env)]
    (if (re-match? form-url-encoded-re ctype)
      (query-parse (body-str env)))))

(def #^{:private true
        :doc "Multipart parsing handler. Saves all multipart param values
              as tempfiles, regardless of size."}
  disk-file-item-factory
  (doto (DiskFileItemFactory.)
    (.setSizeThreshold -1)
    (.setRepository (java.io.File. "/Users/mmcgrana/Desktop/git/clj-garden/weld-upload-example/public/uploads"))
    (.setFileCleaningTracker nil)))

(defn multipart-params
  "Returns a hash of multipart params if the content-type indicates a multipart
  request, nil otherwise."
  [env]
  (if-let [ctype (content-type env)]
    (if (re-match? multipart-re ctype)
      (let [upload  (FileUpload. disk-file-item-factory)
            context (proxy [RequestContext] []
                      (getContentType       [] (content-type env))
                      (getContentLength     [] (content-length env))
                      (getCharacterEncoding [] (character-encoding env))
                      (getInputStream       [] (:body env)))
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
  "Returns a hash of mock params given directly in the env, if any.
  Used for testing."
  [env]
  (:weld.request/mock-params env))

(defn params*
  "Returns all params except for those determined from the route."
  [env]
  (merge (query-params     env)
         (form-params      env)
         (multipart-params env)
         (mock-params      env)))

(defn params
  "Returns params, including those determined from the route.
  If a single arg is given, an env, returns all such params.
  If additional args are given, they are used to get-in these params"
  ([env]
     (merge (params* env) (:weld.request/route-params env)))
  ([env & args]
   (get-in (params env) args)))

(defn request-method*
  "Returns the literal request method indicated in the env, before taking
  into account piggybacking."
  [env]
  (:request-method env))

(defn request-method
  "Returns a Keyword indicating the method for the request, either as literally
  indicated by the request or if available by the _method piggyback param.
  Throws if no valid method is recognized."
  [env]
  (let [r-method (request-method* env)]
    (cond
      (recognized-nonpiggyback-methods r-method)
        r-method
      (= :post r-method)
        (if-let [p-method (:_method (params* env))]
          (or (recognized-nonpiggyback-methods (keyword p-method)) :post)
          :post)
      :else
        (throwf "Unrecognized :request-method %s" r-method))))

(defn scheme
  "Returns a String inidcating the scheme for the request, like http or https."
  [env]
  (:scheme env))

(defn ssl?
  "Returns tree iff the request was submitted made over ssl."
  [env]
  (= "https" (scheme env)))

(defn server-port
  "Returns a Integer for the server port."
  [env]
  (:server-port env))

(defn server-host
  "Returns a String for the full hostname, excluding the port."
  [env]
  (let [hdrs (headers env)]
    (or (get hdrs "x-forwarded-host")
        (get hdrs "host")
        (:server-name env))))

(defn full-uri
  "Returns a String for the full request uri, including the protocol and host
  but excluding the port."
  [env]
  (str (scheme env) "://" (server-host env) (uri env)))

(defn user-agent
  "Returns a String for the http user agent."
  [env]
  (get-in env [:headers "user-agent"]))

(defn ajax?
  "Returns true if the given request was an AJAX request."
  [env]
  (if-let [xrw (get-in env [:headers "x-requested-with"])]
    (re-match? ajax-http-request-re xrw)))

(defn remote-ip
  "Returns a String representing our best guess for the IP of the requesting 
  user."
  [env]
  (let [headers (:headers env)]
    (or (get headers "client-ip")
        (if-let [forwarded (get headers "x-forwarded-for")]
          (let [all-ips       (re-split #"," forwarded)
                remote-ips (remove #(re-match? local-ip-re %) all-ips)]
            (if (not (empty? remote-ips)) (trim (first remote-ips)))))
        (:remote-addr env))))

(defn referrer
  "Returns a String for the http refer(r)er"
  [env]
  (get-in env [:headers "http-referer"]))

(defn assoc-route-params
  "Returns a new request object like the corresponding to the given one but 
  including the given route-params."
  [env route-params]
  (assoc env :weld.request/route-params route-params))