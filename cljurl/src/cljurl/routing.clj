(ns cljurl.routing
  (:use clj-routing.core
        clojure.contrib.def
        cljurl.utils)
  (:require [cljurl.config :as config]))

(defvar- c 'cljurl.app.controllers)

(def routes
  [[c 'index          :index     :get  "/"                  ]
   [c 'new            :new       :get  "/new"               ]
   [c 'create         :create    :put  "/"                  ]
   [c 'show           :show      :get  "/show/:slug"        ]
   [c 'expand         :expand    :get  "/:slug"             ]
   [c 'page-not-found :not_found :any  "/:path" {:path ".*"}]])

;(defrouting routes)

; Dependent on routes, compiler
(defvar- symbolic-recognizer
  (compile-recognizer
    (map (fn [[ns-sym fn-sym name method path opts]]
           [[ns-sym fn-sym] method path opts])
         routes))
  "An fn that returns a [[ns-sym fn-sym] params] tuple based on a given
  method and path.")

; Dependent on symbolic-recognizer
(defn action-recognizer
  "Returns an [action-fn params] tuple corresponding the the given http method
  and path, taking advantage of dynamic resolution."
  [method path]
  (let [[[ns-sym fn-sym] params] (symbolic-recognizer method path)]
    [(ns-resolve ns-sym fn-sym) params]))

; Dependent on routes, compiler
(defvar path-info
  (compile-generator
    (map (fn [[ns-sym fn-sym name method path opts]]
           [name method path opts])
         routes))
  "Returns a [method path unused-params] tuple based on the given 
  name and params.")

; Dependent on config/*app-host*
(defn- absolutize
  "Returns a full url corresponding to the given path, based on the app's
  configured *app-root*."
  [path]
  (str config/*app-host* path))

; Dependent on path-info
(defn path
  "Returns a path based on the given action name and params."
  [name & [params]]
  (second (path-info name params)))

; Dependent on path-info, absolutize
(defn url-info
  "Returns a [method url unused-params] tuple based on the given action name and 
  params"
  [name params]
  (let [path-tuple (path-info name params)]
    (update path-tuple 1 absolutize)))

; Dependent on url-info.
(defn url
  "Returns a full url based on the given action name and params."
  [name params]
  (second (url-info name params)))



