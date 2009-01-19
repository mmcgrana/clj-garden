(ns clj-http-client.core
  (:use (clojure.contrib fcase except def))
  (:import (org.apache.commons.httpclient
             HttpClient DefaultHttpMethodRetryHandler Header
             HttpMethod HttpMethodBase)
           (org.apache.commons.httpclient.methods
             HeadMethod GetMethod PutMethod PostMethod DeleteMethod
             RequestEntity ByteArrayRequestEntity FileRequestEntity
             InputStreamRequestEntity StringRequestEntity EntityEnclosingMethod)
           (org.apache.commons.httpclient.methods.multipart
             MultipartRequestEntity)
           (org.apache.commons.httpclient.params HttpMethodParams)
           (org.apache.commons.httpclient.cookie CookiePolicy)
           (org.apache.commons.io IOUtils)
           (java.io File InputStream)))

(defn- headers-map
  "Returns a header Map with string keys and string values, for the given array
  of Headers from a response."
  [headers-arr]
  (reduce
    (fn [memo #^Header header]
      (assoc memo (.getName header) (.getValue header)))
    {}
    headers-arr))

(defn- apply-headers
  "Add the headers in the given Map to the given http method."
  [#^HttpMethod method headers]
  (doseq [[name value] headers]
    (.setRequestHeader method name value)))

(defvar- ByteArray (class (make-array (Byte/TYPE) 0))
  "Class for primitive byte arrays.")

(defn- #^RequestEntity request-entity
  "Returns an instance implementing RequestEntity for use in put/post requests."
  [[body & [content-type & [encoding]]]]
  (instance-case body
    String      (StringRequestEntity.      body content-type encoding)
    File        (FileRequestEntity.        body content-type)
    ByteArray   (ByteArrayRequestEntity.   body content-type)
    InputStream (InputStreamRequestEntity. #^InputStream body
                                           #^String content-type)
    (throwf "Unrecognized body: %s" body)))

(defn- method-body
  "Returns the response body for the given method as a String"
  [#^HttpMethodBase method]
  (when-let [rbody (.getResponseBodyAsStream method)]
    (IOUtils/toString rbody (.getResponseCharSet method))))

(defn- http-execute-method
  "Generalized http request."
  [#^HttpMethod method headers body-args handler]
  (let [client        (HttpClient.)
        method-params (.getParams method)]
    (.setParameter method-params HttpMethodParams/RETRY_HANDLER
      (DefaultHttpMethodRetryHandler.))
    (.setCookiePolicy method-params CookiePolicy/IGNORE_COOKIES)
    (when headers (apply-headers method headers))
    (when body-args
      (.setRequestEntity #^EntityEnclosingMethod method
        (request-entity body-args)))
    (try
      (let [status       (.executeMethod client method)
            headers      (headers-map (.getResponseHeaders method))]
        (handler status headers method))
      (finally
        (.releaseConnection method)))))

(defn http-head
  "Returns a [status headers] tuple corresponding to the response from a 
  HEAD request to the given url, optionally qualified by the given headers."
  [url & [headers]]
  (http-execute-method (HeadMethod. url) headers nil
    (fn [h s method] [h s])))

(defn http-get
  "Returns a [status headers body-string] tuple corresponding to the response
  from the given url."
  [url & [headers]]
  (http-execute-method (GetMethod. url) headers nil
    (fn [s h method] [s h (method-body method)])))

(defn http-get-bytes
  "Returns a [status headers body-byte-array] tuple corresponding to the 
  response from the given url."
  [url & [headers]]
  (http-execute-method (GetMethod. url) headers nil
    (fn [s h #^HttpMethod method]
      [s h (IOUtils/toByteArray (.getResponseBodyAsStream method))])))

(defn http-get-stream
  "Experimental. Invokes the given handler with the status, headers, and 
  response body input stream for a request to the given url with the given 
  headers."
  ([url headers handler]
   (http-execute-method (GetMethod. url) headers nil
     (fn [s h #^HttpMethod get-method]
       (with-open [b-stream (.getResponseBodyAsStream get-method)]
         (handler s h b-stream)))))
  ([url handler]
   (http-get-stream url {} handler)))

(defn- headers-and-body-args
  "Returns a tuple of [headers body-args] corresponding to the given
  request-args"
  [request-args]
  (if (map? (first request-args))
    [(first request-args) (rest request-args)]
    [nil request-args]))

(defn http-put
  "Returns a [status headers body] tuple corresponding to the response from a
  PUT request to the given url. request-args are of the form 
  [headers? put-entity content-type? encoding?]"
  [url & request-args]
  (let [[headers body-args] (headers-and-body-args request-args)]
    (http-execute-method (PutMethod. url) headers body-args
      (fn [s h put-method] (method-body put-method)))))

(defn http-post
  "Returns a [status headers body] tuple corresponding to the response from a
  POSt request to the given url. request-args are of the form 
  [headers? post-entity content-type? encoding?]"
  [url & request-args]
  (let [[headers body-args] (headers-and-body-args request-args)]
    (http-execute-method (PostMethod. url) headers body-args
      (fn [s h post-method] [s h (method-body post-method)]))))

(defn http-delete
  "Returns a [status headers body] tuple corresponding to the response
  from a DELETE request to the given url, optionally qualified by the given
  headers."
  [url & [headers]]
  (http-execute-method (DeleteMethod. url) headers nil
    (fn [s h delete-method] [s h (method-body delete-method)])))
