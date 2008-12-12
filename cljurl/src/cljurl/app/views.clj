(ns cljurl.app.views
  (:use cljurl.routing
        clj-html.core
        clj-html.helpers))

(defn layout
  "Layout shared by all templates."
  [content]
  (html
    (doctype :xhtml-transitional)
    [:html {:xmlns "http://www.w3.org/1999/xhtml"}
      [:head
        [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
        [:title "cljurl"]
        [:body content]]]))

(defn index
  "Home page - user enters a url to shorten."
  []
  (layout
    (html
      (form-to (path-info :create)
        (text-field "url" "Enter url here")))))

(defn show
  "Results page - users sees their shortened url."
  [shortening]
  (layout
    (html
      [:p
        "Url shortened from " (h (:url shortening)) " to "
        (h (url :expand shortening))])))

(defn not-found
  "404 Not Found page."
  []
  (layout
    "O noes - we couldn't find that"))