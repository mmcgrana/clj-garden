(ns cljurl.routing
  (:require
    [ring.routing  :as routing]
    [cljurl.config :as config]))

(def c 'cljurl.controllers)

(def routes
  [[c 'index          :index         :get  "/"                     ]
   [c 'new            :new           :get  "/new"                  ]
   [c 'create         :create        :put  "/"                     ]
   [c 'show           :show          :get  "/show/:slug"           ]
   [c 'expand         :expand        :get  "/:slug"                ]
   [c 'expand-api     :expand-api    :get  "/:slug.js"             ]
   [c 'not-found-api  :not-found-api :any  "/:path.js" {:path ".*"}]
   [c 'not-found      :not-found     :any  "/:path"    {:path ".*"}]])

(routing/defrouting
  config/+app-host+
  routes)
