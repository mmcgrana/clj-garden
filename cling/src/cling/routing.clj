(ns cling.routing
  (:use
    (weld routing)))

(defn s [sym] (symbol "cling.controllers" (str sym)))

(def router
  (compiled-router
    [[(s 'home)         :home                  :get    "/"                 ]
     [(s 'index-pages)  :index-pages           :get    "/pages"            ]
     [(s 'new-page)     :new-page              :get    "/pages/new"        ]
     [(s 'show-page)    :show-page             :get    "/pages/:permalink" ]
     [(s 'edit-page)    :edit-page             :get    "/pages/:id/edit"   ]
     [(s 'create-page)  :create-page           :put    "/pages"            ]
     [(s 'update-page)  :update-page           :post   "/pages/:id"        ]
     [(s 'destroy-page) :destroy-page          :delete "/pages/:id"        ]
     [(s 'not-found)    :not-found             :any    "/:path" {:path ".*"}]]))
