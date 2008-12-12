(ns cwsg.apps.dump
  (:use clj-html.core clj-html.helpers))

(defn- template
  [env]
  (html
    (doctype :xhtml-transitional)
    [:html {:xmlns "http://www.w3.org/1999/xhtml"}
      [:head
        [:meta {:http-equiv "Content-Type" :content "text/html"}]
        [:title "cwsg env dump"]]
      [:body
        [:pre
          (pr-str env)]]]))

(defn app
  "Returns a response tuple corresponding to an HTML dump of the request
  env as it was recieved by this app."
  [env]
  [200
   {"Content-Type" "text/html"}
   (template env)])