(ns ring.controller
  (:import (org.apache.commons.io FilenameUtils)))

(defn not-found
  "Reterns a tuple for a 400 html response with the given html content."
  [content]
  [404 {"Content-Type" "text/html"} content])

(defn internal-error
  [content]
  [500 {"Content-Type" "text/html"} content])

(defn render
  "Returns a tuple for a 200 html reponse with the given html content."
  [content]
  [200 {"Content-Type" "text/html"} content])

(defn redirect
  "Returns a tuple for a redirect. Status is from :status in options or by
  default 302."
  [url & [options]]
  [(get options :status 302)
   {"Location" url}
   (str "You are being <a href=\"" url "\">redirected</a>.")])

(defn send-file
  "Returns a response tuple that will cause the client to download the given
  file."
  [file & [options]]
  (let [filename (get options :filename (FilenameUtils/getName (.getPath file)))]
    [200
     {"Content-Transfer-Encoding" "binary"
      "Content-Disposition"       (str "attachment; filename=" filename)}
     file]))