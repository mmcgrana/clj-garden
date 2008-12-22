(ns cljurl.app.views
  (:use cljurl.routing
        clj-html.core
        clj-html.helpers
        [stash.core :only (errors)])
  (:load "view_helpers"))

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

(defn not-found
  "404 Not Found page."
  []
  (with-layout
    "Were sorry - we couldn't find that."))

(defn internal-error
  "500 Internal Error page"
  []
  (with-layout
    "Were sorry - something went wrong."))