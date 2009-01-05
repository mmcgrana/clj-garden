(ns cljre.app
  (:use
    (ring app controller request)
    (clj-html core helpers)
    (cljre helpers))
  (:require
    ring.routing
    [cwsg.middleware.reloading             :as reloading]
    [cwsg.middleware.show-exceptions       :as show-exceptions]
    [cwsg.middleware.file-content-info     :as file-content-info]
    [cwsg.middleware.static                :as static]))

(def +app-host+ "http://cljre.com")
(def +show-exceptions+ true)
(def +public-dir+ (java.io.File. "public"))

(defrouting
  +app-host+
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
         (include-css "/stylesheets/reset-min.css"
                      "/stylesheets/main.css")
         (include-js  "/javascripts/jquery-1.2.6.js"
                      "/javascripts/main.js")
         [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
         [:meta {:name "keywords" :content "regex, regexp, regular expression, clojure, open source"}]
         [:meta {:name "description" :content "Edit and test Clojure regexs in your browser with realtime match updates. Open source."}]
         [:title "cljre: a Clojure Regex Editor"]]
       [:body ~@body]]))

(defn vindex []
  (with-layout
    [:div#container
      [:div#header
        [:h1 [:a {:href "/" :title "cljre"} "cljre"] ": a Clojure Regex Editor"]]
      [:div#editor
        [:p.label "pattern:"]
        (text-area-tag "pattern" {:id "pattern" :rows 1 :spellcheck "false"})
        [:p.label "string:"]
        (text-area-tag "string"  {:id "string" :rows 3 :spellcheck "false"})
        [:p.label "re-seq result:"]
        (text-area-tag "result" {:id "result" :rows 3 :spellcheck "false"})]
      [:div#footer
        [:p "An " [:a {:href "http://github.com/mmcgrana/cljre/tree/master" :title "cljre code on GitHub"} "open source"]
            " Clojre app inspired by " [:a {:href "http://lovitt.net/" :title "Michael Lovitt"} "Michael Lovitt"] "'s "
            [:a {:href "http://rubular.com" :title "Rubular"} "Rubular"] "."]]]))

(defn index [req]
  (respond (vindex)))

(defn match-data [string pattern-str]
  (try
    (let [pattern   (re-pattern pattern-str)
          matches   (re-seq pattern string)]
      (if matches
        {:status "match" :result (prn-str matches)}
        {:status "no-match"}))
    (catch java.util.regex.PatternSyntaxException e
      {:status "syntax-error" :message (.getMessage e)})))

(defn match [req]
  (respond-json (match-data (params req :string) (params req :pattern))))

(def app
  (show-exceptions/wrap #(identity +show-exceptions+)
    (file-content-info/wrap
      (static/wrap +public-dir+
        (reloading/wrap #(list 'cljre.app)
          (spawn-app router))))))