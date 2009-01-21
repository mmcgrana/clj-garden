(ns weldblog.routing
  (:use weld.routing))

(defn s [sym] (symbol "weldblog.controllers" (str sym)))

(def router
  (compiled-router
    [[(s 'new-session)      :new-session      :get    "/sessions/new"]
     [(s 'create-session)   :create-session   :put    "/sessions"    ]
     [(s 'destroy-session)  :destroy-session  :delete "/sessions"    ]
     [(s 'index-posts)      :index-posts      :get    "/"            ]
     [(s 'index-posts-atom) :index-posts-atom :get    "/posts.atom"  ]
     [(s 'new-post)         :new-post         :get    "/new"         ]
     [(s 'show-post)        :show-post        :get    "/:id"         ]
     [(s 'edit-post)        :edit-post        :get    "/:id/edit"    ]
     [(s 'create-post)      :create-post      :put    "/"            ]
     [(s 'update-post)      :update-post      :post   "/:id"         ]
     [(s 'destroy-post)     :destroy-post     :delete "/:id"         ]
     [(s 'not-found)        :not-found        :any    "/:path" {:path ".*"}]]))
