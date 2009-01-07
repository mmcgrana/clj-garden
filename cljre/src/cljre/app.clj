(ns cljre.app
  (:use
    (ring controller request)
    (clj-html core helpers helpers-ext))
  (:require
    [org.danlarkin.json                    :as json]
    [ring.routing                          :as routing]
    [cwsg.middleware.reloading             :as reloading]
    [cwsg.middleware.show-exceptions       :as show-exceptions]
    [cwsg.middleware.file-content-info     :as file-content-info]
    [cwsg.middleware.static                :as static]
    [ring.app                              :as app]))

;; Config
(def +app-host+ "http://cljre.com")
(def +public-dir+ (java.io.File. "public"))
(def +reloadable-namespace-syms+ '(cljre.app))

;; Routing
(routing/defrouting
  +app-host+
  [['cljre.app 'index     :index     :get  "/"]
   ['cljre.app 'match     :match     :post "/match"]
   ['cljre.app 'not-found :not-found :any  "/:path" {:path ".*"}]])

;; Views
(defmacro with-layout
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

(defn index-view []
  (with-layout
    [:div#container
      [:div#header
        [:h1 [:a {:href "/" :title "cljre"} "cljre"] ": a Clojure Regex Editor"]]
      [:div#editor
        [:p.label "pattern:"]
        (text-area-tag "pattern" nil {:id "pattern" :rows 1 :spellcheck "false"})
        [:p.label "string:"]
        (text-area-tag "string" nil {:id "string" :rows 3 :spellcheck "false"})
        [:p.label "re-seq result:"]
        (text-area-tag "result" nil {:id "result" :rows 3 :spellcheck "false"})]
      [:div#footer
        [:p "An " [:a {:href "http://github.com/mmcgrana/cljre/tree/master" :title "cljre code on GitHub"} "open source"]
            " Clojre app inspired by " [:a {:href "http://lovitt.net/" :title "Michael Lovitt"} "Michael Lovitt"] "'s "
            [:a {:href "http://rubular.com" :title "Rubular"} "Rubular"] "."]]]))

(defn match-data [pattern-str string]
  (try
    (let [pattern   (re-pattern pattern-str)
          matches   (re-seq pattern string)]
      (if matches
        {:status "match" :result (pr-str matches)}
        {:status "no-match"}))
    (catch java.util.regex.PatternSyntaxException e
      {:status "syntax-error" :message (.getMessage e)})))

(defn match-view [pattern-str string]
  (json/encode-to-str (match-data pattern-str string) :indent 2))

(defn not-found-view []
  (with-layout
    [:div#container
      "404 Page Not Found"]))

;; Controllers
(defn index [req]
  (respond (index-view)))

(defn match [req]
  (respond
    (match-view (params req :pattern) (params req :string))
    {:content-type "text/javascript"}))

(defn not-found [req]
  (respond-404 (not-found-view)))

;; CWSG app
(defn build-app [env]
  (app/wrap-if (= env :dev)
    show-exceptions/wrap
    (file-content-info/wrap
      (static/wrap +public-dir+
        (app/wrap-if (= env :dev)
          (partial reloading/wrap +reloadable-namespace-syms+)
          (app/spawn-app router))))))
