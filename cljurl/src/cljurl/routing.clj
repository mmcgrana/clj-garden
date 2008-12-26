(ns cljurl.routing
  (:use ring.routing)
  (:require [cljurl.config :as config]))

(def c 'cljurl.app.controllers)

(def routes
  [[c 'index          :index     :get  "/"                  ]
   [c 'new            :new       :get  "/new"               ]
   [c 'create         :create    :put  "/"                  ]
   [c 'show           :show      :get  "/show/:slug"        ]
   [c 'expand         :expand    :get  "/:slug"             ]
   [c 'page-not-found :not-found :any  "/:path" {:path ".*"}]])

(def router (compiled-router routes config/*app-host*))
