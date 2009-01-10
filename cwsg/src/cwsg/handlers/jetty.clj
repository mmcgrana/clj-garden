(ns cwsg.handlers.jetty
  (:import (javax.servlet.http HttpServletRequest HttpServletResponse)
           (org.mortbay.jetty.handler AbstractHandler)
           (org.mortbay.jetty Server)
           (java.io File FileInputStream InputStream OutputStream)
           (org.apache.commons.io IOUtils))
  (:use (clojure.contrib fcase except)))

(defn- env-map
  "Returns a map representing the given request, to be passed as the env
  to an app."
  [#^HttpServletRequest request]
  {:cwsg/uri                (.getRequestURI request)
   :cwsg/query-string       (.getQueryString request)
   :cwsg/scheme             (.getScheme request)
   :cwsg/request-method     (keyword (.toLowerCase (.getMethod request)))
   :cwsg/content-type       (.getContentType request)
   :cwsg/content-length     (let [len (.getContentLength request)]
                         (if (>= len 0) len))
   :cwsg/character-encoding (.getCharacterEncoding request)
   :cwsg/server-port        (.getServerPort request)
   :cwsg/server-name        (.getServerName request)
   :cwsg/remote-addr        (.getRemoteAddr request)
   :cwsg/headers            (reduce
                         (fn [header-map #^String header-name]
                           (assoc header-map
                             (.toLowerCase header-name)
                             (.getHeader request header-name)))
                         {}
                         (enumeration-seq (.getHeaderNames request)))
   :cwsg/input              (.getInputStream request)}))

(defn- apply-response-tuple
  "Apply the given [status headers body] response tuple to the given response."
  [#^HttpServletResponse response [status headers body]]
  ; Apply the status.
  (.setStatus response status)
  ; Apply the headers.
  (doseq [[key val-or-vals] headers]
    (if (string? val-or-vals)
      (.setHeader response key val-or-vals)
      (doseq [val val-or-vals]
        (.addHeader response key val))))
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

(defn- proxy-handler
  "Returns an Handler implementation for the given app."
  [app]
  (proxy [AbstractHandler] []
    (handle [target request response dispatch]
      (let [env   (env-map request)
            tuple (app env)]
        (apply-response-tuple response tuple)
        (.setHandled request true)))))

(defn run
  "Serve the given app according to the given options.
  Options currently consists only of :port, an integer."
  [options app]
  (let [port    (or (:port options) (throwf ":port missing from options"))
        server  (doto (Server. port) (.setSendDateHeader true))
        handler (proxy-handler app)]
    (.setHandler server handler)
    (.start server)
    (.join  server)))
