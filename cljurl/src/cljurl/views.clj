(ns cljurl.views
  (:use cljurl.routing
        (clj-html core helpers helpers-ext)
        [stash.core :only (errors)])
  (:require [org.danlarkin.json :as json]))

(defn str-json
   "Encode the Clojure data structure as a JSON string with 2 spaces of indent."
   [data]
   (json/encode-to-str data :indent 2))

(defn layout
  "Layout shared by all templates."
  [content]
  (html
    (doctype :xhtml-transitional)
    [:html {:xmlns "http://www.w3.org/1999/xhtml"}
      [:head
        (include-css "/stylesheets/main.css")
        [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
        [:title "cljurl"]
        [:body content]]]))

(defmacro with-layout
  [& body]
  `(layout
     (html ~@body)))

(defn index
  "Home page - List recent shortenings."
  [shortenings]
  (with-layout
    (link-tag "new shortening" (path :new))
    [:h3 "Recent Shortenings"]
    (domap-str [shortening shortenings]
      (html
        [:p (h (:slug shortening)) " => " (h (:url shortening))]))))

(defn error-messages-for
  "Returns html snippet about what needs fixing in shortening, if anything."
  [shortening]
  (if-let [errs (errors shortening)]
    (domap-str [err errs]
      (html [:p (hstr (:on err)) " -- " (hstr (:cause err))]))))

(defn new
  "New form - shortening may be brand new or based on an erroneous submission."
  [shortening]
  (with-layout
    (error-messages-for shortening)
    (form-to (path-info :create)
      (html
        [:p "Enter url:"]
        (text-field-tag "shortening[url]" (:url shortening))
        (submit-tag "Submit")))))

(defn show
  "Results page - users sees their shortened url."
  [shortening]
  (with-layout
    [:p
      "Url shortened from " (h (:url shortening)) " to "
      (h (url :expand shortening))]))

(defn expand-api
  "Render a shortening expansion JSON string."
  [shortening]
  (str-json {:url (:url shortening)}))

(defn not-found
  "404 page."
  []
  (with-layout
    "We're sorry, we could not find that."))

(defn not-found-api
  "404 JSON for api."
  []
  (str-json {:error "404 Not Found"}))

(defn internal-error
  "500 reponse page."
  []
  (with-layout
    "We're sorry, something went wrong."))

(defn internal-error-api
  "500 Response body for api."
  []
  (str-json {:error "500 Internal Error"}))