(ns weldblog.routing
  (:use weld.routing))

(defn s [sym] (symbol "weldblog.controllers" (str sym)))

(def router
  (compiled-router
    [[(s 'home)             :home                  :get    "/"                ]
     [(s 'new-session)      :new-session           :get    "/sessions/new"    ]
     [(s 'create-session)   :create-session        :put    "/sessions"        ]
     [(s 'destroy-session)  :destroy-session       :delete "/sessions"        ]
     [(s 'index-posts)      :index-posts           :get    "/posts"           ]
     [(s 'index-posts)      :index-posts-paginated :get    "/posts/page/:page"]
     [(s 'index-posts-atom) :index-posts-atom      :get    "/posts.atom"      ]
     [(s 'new-post)         :new-post              :get    "/posts/new"       ]
     [(s 'show-post)        :show-post             :get    "/posts/:id"       ]
     [(s 'edit-post)        :edit-post             :get    "/posts/:id/edit"  ]
     [(s 'create-post)      :create-post           :put    "/posts"           ]
     [(s 'update-post)      :update-post           :post   "/posts/:id"       ]
     [(s 'destroy-post)     :destroy-post          :delete "/posts/:id"       ]
     [(s 'not-found)        :not-found             :any    "/:path" {:path ".*"}]]))
