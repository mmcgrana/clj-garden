(ns cwsg.apps.dump
  (:use (clj-html core helpers))
  (:import (org.apache.commons.io IOUtils)))

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
          (domap-str [[key value] (sort-by key env)]
            (h (str key "\n  " (prn-str value) "\n")))]
        [:h3 "Body contents:"]
          (prn-str (IOUtils/toString ((:stream-fn env))))]]))

(defn app
  "Returns a response tuple corresponding to an HTML dump of the request
  env as it was recieved by this app."
  [env]
  [200
   {"Content-Type" "text/html"}
   (template env)])