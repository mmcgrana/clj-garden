(ns ringblog.routing
  (:require
    [ring.routing    :as routing]
    [ringblog.config :as config]))

(def c 'ringblog.controllers)

(router/defrouting config/app-host
  [[c 'index      :index     :get "/"                  ]
    [c 'new       :new       :get "/new"               ]
    [c 'create    :create    :put "/"                  ]
    [c 'show      :show      :get "/:id"               ]
    [c 'not-found :not-found :any "/:path" {:path ".*"}]])
