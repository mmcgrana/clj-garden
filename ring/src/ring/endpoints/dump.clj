(ns ring.endpoints.dump
  (:use (clj-html core helpers)
        clojure.contrib.def)
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
          (prn-str (IOUtils/toString (:body env)))]]))

(defn app
  "Returns a response tuple corresponding to an HTML dump of the request
  env as it was recieved by this app."
  [env]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (template env)})