(ns cljurl.routing
  (:use clj-routing.core
        clojure.contrib.def)
  (:require [cljurl.config :as config]))

(defvar- c 'cljurl.app.controllers)

(def routes
  [[c 'index          :index     :get  "/"                  ]
   [c 'create         :create    :put  "/"                  ]
   [c 'show           :show      :get  "/info/:slug"        ]
   [c 'expand         :expand    :get  "/:slug"             ]
   [c 'page-not-found :not_found :any  "/:path" {:path ".*"}]])

(defvar- symbolic-recognizer
  (compile-recognizer
    (map (fn [[ns-sym fn-sym name method path opts]]
           [[ns-sym fn-sym] method path opts])
         routes)))

(defn action-recognizer
  "Returns an [action-fn params] tuple corresponding the the given http method
  and path."
  [method path]
  (let [[[ns-sym fn-sym] params] (symbolic-recognizer method path)]
    [(ns-resolve ns-sym fn-sym) params]))

(defvar path-info
  (compile-generator
    (map (fn [[ns-sym fn-sym name method path opts]]
           [name method path opts])
         routes))
  "Returns a [method path unused-params] tuple based on the given 
  action-fn and params.")

(defn path
  "Returns a path based on the given action name and params."
  [name params]
  (second (path-info name params)))

(defn- absolutize
  "Returns a full url corresponding to the given path, based on the app's
  configured *app-root*."
  [path]
  (str config/*app-host* path))

(defn url-info
  "Returns a [method url unused-params] tuple based on the given action name and 
  params"
  [name params]
  (let [path-tuple (path-info name params)]
    (update-in path-tuple [1] absolutize)))

(defn url
  "Returns a full url based on the given action name and params."
  [name params]
  (second (url-info name params)))
