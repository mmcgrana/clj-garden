(ns cljurl.routing
  (:require ring.routing [cljurl.config :as config]))

(def c 'cljurl.app.controllers)

(def routes
  [[c 'index          :index     :get  "/"                  ]
   [c 'new            :new       :get  "/new"               ]
   [c 'create         :create    :put  "/"                  ]
   [c 'show           :show      :get  "/show/:slug"        ]
   [c 'expand         :expand    :get  "/:slug"             ]
   [c 'page-not-found :not-found :any  "/:path" {:path ".*"}]])

(def router (ring.routing/compiled-router routes config/+app-host+))

(def path-info (partial ring.routing/path-info router))
(def path      (partial ring.routing/path      router))
(def url-info  (partial ring.routing/url-info  router))
(def url       (partial ring.routing/url       router))