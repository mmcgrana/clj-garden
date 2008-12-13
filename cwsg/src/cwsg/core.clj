(ns cwsg.core
  (:import (javax.servlet.http HttpServlet
                               HttpServletRequest
                               HttpServletResponse)
           (org.mortbay.jetty Server)
           (org.mortbay.jetty.servlet Context ServletHolder)
           (java.io File FileInputStream InputStream OutputStream)
           (org.apache.commons.io IOUtils))
  (:use clojure.contrib.fcase clojure.contrib.except))

(defn- env-map
  "Returns a map representing the given request, to be passed as the env
  to an app."
  [#^HttpServletRequest request]
  (let [content-length   (.getContentLength request)]
    {:uri                (.getRequestURI request)
     :query-string       (.getQueryString request)
     :scheme             (.getScheme request)
     :request-method     (keyword (.toLowerCase (.getMethod request)))
     :content-type       (.getContentType request)
     :content-length     (let [len (.getContentLength request)]
                           (if (<= 0 len) len))
     :character-encoding (.getCharacterEncoding request)
     :server-port        (.getServerPort request)
     :server-name        (.getServerName request)
     :remote-addr        (.getRemoteAddr request)
     :headers            (reduce
                           (fn [header-map #^String header-name]
                             (assoc header-map
                               (.toLowerCase header-name)
                               (.getHeader request header-name)))
                           {}
                           (enumeration-seq (.getHeaderNames request)))
     :reader-fn          #(.getReader request)
     :stream-fn          #(.getInputStream request)}))

(defn- apply-response-tuple
  "Apply the given [status headers body] response tuple to the given response."
  [#^HttpServletResponse response [status headers body]]
  ; Apply the status.
  (.setStatus response status)
  ; Apply the headers.
  (doseq [[header-name header-value] headers]
    (.setHeader response header-name header-value))
  ; Apply the body - the method depends on the given body type.
  (instance-case body
    String
      (with-open [writer (.getWriter response)]
        (.println writer body))
    InputStream
      (let [#^InputStream in body]
        (with-open [out (.getOutputStream response)]
          (IOUtils/copy in out)
          (.close in)
          (.flush out)))
    File
      (let [#^File f body]
        (with-open [fin (FileInputStream. f)]
          (with-open [out (.getOutputStream response)]
            (IOUtils/copy fin out)
            (.flush out))))
    (throwf "Unreceognized body: %s" body)))

(defn- proxy-servlet
  "Returns an HttpServlet implementation for the given app."
  [app]
  (proxy [HttpServlet] []
    (service [request response]
      (let [env   (env-map request)
            tuple (app env)]
        (apply-response-tuple response tuple)))))

(defn serve
  "Serve the given app according to the given options.
  Options must at least include :port, and int."
  [options app]
  (let [#^HttpServlet servlet (proxy-servlet app)
        port    (or (:port options) (throwf ":port missing from options"))
        server  (doto (Server. port) (.setSendDateHeader true))
        context (Context. server "/" false false)]
    (.addServlet context (ServletHolder. servlet) "/")
    (.start server)))