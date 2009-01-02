(ns cljre.app
  (:use
    (ring app controller request)
    (clj-html core helpers)
    cljre.helpers)
  (:require
    ring.routing
    [cwsg.middleware.show-exceptions       :as show-exceptions]
    [cwsg.middleware.file-content-info     :as file-content-info]
    [cwsg.middleware.static                :as static]
    cljre.helpers))

(def +app-host+ "http://cljre.com")
(def +show-exceptions+ true)
(def +public-dir+ (java.io.File. "public"))

(defrouting +app-host+
  [['cljre.app 'index     :index     :get  "/"]
   ['cljre.app 'match     :match     :post "/match"]
   ['cljre.app 'not-found :not-found :any  "/:path" {:path ".*"}]])

(defmacro with-layout
  "Layout shared by all templates."
  [& body]
  `(html
     (doctype :xhtml-transitional)
     [:html {:xmlns "http://www.w3.org/1999/xhtml"}
       [:head
         (include-css "/stylesheets/main.css")
         (include-js "/javascripts/jquery-1.2.6.js")
         [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
         [:title "cljre"]]
       [:body ~@body]]))

(defn vindex []
  (with-layout
    (form-to (path-info :match)
      (html
        (text-field-tag "pattern" {:id "pattern"})
        (text-field-tag "string"  {:id "string"})
        (text-field-tag "matches" {:id "matches"})))))

(defn index [req]
  (respond (vindex)))

(defn match-js [matches]
  "$('matches').val(" (h (pr-str matches)) ")")

(defn match [req]
  (let [pattern   (re-pattern (params req :regex))
        string    (params req :string)
        matches   (re-seq re-pattern string)]
    (respond-js (match-js matches))))

(def app
  (show-exceptions/wrap #(identity +show-exceptions+)
    (file-content-info/wrap
      (static/wrap +public-dir+
        (spawn-app router)))))