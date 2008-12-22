(ns cwsg.middleware.show-exceptions
  (:use (clj-html core helpers)
        clojure.contrib.str-utils
        clj-backtrace.core
        clojure.contrib.repl-utils))

(def css "
/*
Copyright (c) 2008, Yahoo! Inc. All rights reserved.
Code licensed under the BSD License:
http://developer.yahoo.net/yui/license.txt
version: 2.6.0
*/
html{color:#000;background:#FFF;}body,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,code,form,fieldset,legend,input,textarea,p,blockquote,th,td{margin:0;padding:0;}table{border-collapse:collapse;border-spacing:0;}fieldset,img{border:0;}address,caption,cite,code,dfn,em,strong,th,var{font-style:normal;font-weight:normal;}li{list-style:none;}caption,th{text-align:left;}h1,h2,h3,h4,h5,h6{font-size:100%;font-weight:normal;}q:before,q:after{content:'';}abbr,acronym{border:0;font-variant:normal;}sup{vertical-align:text-top;}sub{vertical-align:text-bottom;}input,textarea,select{font-family:inherit;font-size:inherit;font-weight:inherit;}input,textarea,select{*font-size:100%;}legend{color:#000;}del,ins{text-decoration:none;}

table.trace {
  background: lightgrey;
}

table.trace tr {
  line-height: 1.4em;
}

table.trace td.source {
  text-align: right;
}

table.trace td.method {
  padding-left: .5em;
  text-aligh: left;
}
")

(defn- exception-name [class]
  (re-sub #"^class\s(java\.lang\.)?" "" (str class)))

(defn source-str [parsed]
  (if (and (:file parsed) (:line parsed))
    (str (:file parsed) ":" (:line parsed))
    "(Unknown Source)"))

(defn- clojure-elem-str [parsed]
  (str (:ns parsed) "/" (:fn parsed) (if (:annon-fn parsed) "[fn]")))

(defn java-elem-str [parsed]
  (str (:class parsed) "." (:method parsed)))

(defn clojure-source [parsed]
  (get-source (symbol (str (:ns parsed) "/" (:fn parsed)))))

(defn- exceptions-response
  "Returns a response showing debugging information about the exception."
  [env e]
  [500 {"Content-Type" "text/html"}
    (html
      (doctype :xhtml-transitional)
      [:html {:xmlns "http://www.w3.org/1999/xhtml"}
        [:head
          [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
          [:title "Show Exceptions"]
          [:style {:type "text/css"} css]
          [:body
            [:div#content
              [:h3 (h (exception-name (class e)))]
              [:h4 (h (.getMessage e))]
              [:table.trace
                (domap-str [parsed (parse-trace (.getStackTrace e))]
                  (html
                    [:tbody
                      (if (:clojure parsed)
                        (html
                          [:tr
                            [:td.source (h (source-str       parsed))]
                            [:td.method (h (clojure-elem-str parsed))]]
                          (if-let [clj-src (clojure-source parsed)]
                            (html [:tr [:td {:colspan 2} [:pre clj-src]]])))
                        (html
                          [:tr
                            [:td.source (h (source-str    parsed))]
                            [:td.method (h (java-elem-str parsed))]]))]))]]]]])])

(defn wrap
  "Returns an app corresponding to the given one but for which catches all
  exceptions thrown within the app and displays helpful debugging information."
  [app]
  (fn [env]
    (try
      (app env)
      (catch Exception e
        (exceptions-response env e)))))
