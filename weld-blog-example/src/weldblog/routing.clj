(ns weldblog.routing
  (:require
    [weld.routing    :as routing]
    [weldblog.config :as config]))

(def c 'weldblog.controllers)

(routing/defrouting config/app-host
  [[c 'index      :posts        :get    "/"                  ]
   [c 'index-atom :posts-atom   :get    "/posts.atom"        ]
   [c 'new        :new-post     :get    "/new"               ]
   [c 'show       :post         :get    "/:id"               ]
   [c 'edit       :edit-post    :get    "/:id/edit"          ]
   [c 'create     :create-post  :put    "/"                  ]
   [c 'update     :update-post  :post   "/:id"               ]
   [c 'destroy    :destroy-post :delete "/:id"               ]
   [c 'not-found  :not-found    :any    "/:path" {:path ".*"}]])
