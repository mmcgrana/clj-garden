(ns weldblog.routing
  (:use weld.routing))

(defn s [sym] (symbol "weldblog.controllers" (str sym)))

(def router
  (compiled-router
    [[(s new-session)     :new-session     :get    "/sessions/new"]
     [(s create-session)  :create-session  :put    "/sessions"    ]
     [(s destroy-session) :destroy-session :delete "/sessions"    ]
     [(s index)           :posts           :get    "/"            ]
     [(s index-atom)      :posts-atom      :get    "/posts.atom"  ]
     [(s new)             :new-post        :get    "/new"         ]
     [(s show)            :post            :get    "/:id"         ]
     [(s edit)            :edit-post       :get    "/:id/edit"    ]
     [(s create)          :create-post     :put    "/"            ]
     [(s update)          :update-post     :post   "/:id"         ]
     [(s destroy)         :destroy-post    :delete "/:id"         ]
     [(s not-found)       :not-found       :any    "/:path" {:path ".*"}]]))
