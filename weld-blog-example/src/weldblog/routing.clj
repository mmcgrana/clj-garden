(ns weldblog.routing
  (:require
    [weld.routing    :as routing]
    [weldblog.config :as config]))

(def c 'weldblog.controllers)

(router/defrouting config/app-host
  [[c 'index      :index     :get "/"                  ]
    [c 'new       :new       :get "/new"               ]
    [c 'create    :create    :put "/"                  ]
    [c 'show      :show      :get "/:id"               ]
    [c 'not-found :not-found :any "/:path" {:path ".*"}]])
