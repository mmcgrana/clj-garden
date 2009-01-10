(ns weld.controller
  (:use weld.utils)
  (:import (org.apache.commons.io FilenameUtils)))

(defn respond
  "Most general function for returning a response tupel.
  For opts, :status defaults to 200 and :content-type to text/html."
  [body & [opts]]
  {:status  (get opts :status 200)
   :headers {"Content-Type" (get opts :content-type "text/html")}
   :body    body})

(defn respond-404
  "Returns a tuple for a 404 response, with other options as specified by
  respond."
  [body & [opts]]
  (respond body (assoc opts :status 404)))

(defn respond-500
  "Returns a tuple for a 500 reponse, with other options as specified by
  respond."
  [body & [opts]]
  (respond body (assoc opts :status 500)))

(defn redirect
  "Returns a tuple for a redirect. 
  For opts, :status is from :status defaults to 302."
  [url & [opts]]
  {:status  (get opts :status 302)
   :headers {"Location" url}
   :body    (str "You are being <a href=\"" url "\">redirected</a>.")})

(defn send-file
  "Returns a response tuple that will cause the client to download the given
  file."
  [file & [opts]]
  (let [filename (get-or opts :filename (FilenameUtils/getName (.getPath file)))]
    {:status  200
     :headers {"Content-Transfer-Encoding" "binary"
               "Content-Disposition"       (str "attachment; filename=" filename)}
     :body    file}))