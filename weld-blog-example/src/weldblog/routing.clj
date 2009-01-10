(ns weldblog.routing
  (:require
    [weld.routing    :as routing]
    [weldblog.config :as config]))

(def c 'weldblog.controllers)

(routing/defrouting config/app-host
  [[c 'index      :posts       :get "/"                  ]
   [c 'index-atom :posts-atom  :get "/posts.atom"        ]
   [c 'new        :new-post    :get "/new"               ]
   [c 'create     :create-post :put "/"                  ]
   [c 'show       :post        :get "/:id"               ]
   [c 'not-found  :not-found   :any "/:path" {:path ".*"}]])
