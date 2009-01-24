(ns cling.routing
  (:use
    (weld routing)))

(defn s [sym] (symbol "cling.controllers" (str sym)))

(def router
  (compiled-router
    [[(s 'home)                      :home                      :get    "/"                                ]
     [(s 'index-pages)               :index-pages               :get    "/pages"                           ]
     [(s 'index-pages-versions)      :index-pages-versions      :get    "/pages/versions"                  ]
     [(s 'index-pages-versions-atom) :index-pages-versions-atom :get    "/pages/versions.atom"             ]
     [(s 'search-pages)              :search-pages              :get    "/pages/search/:query"             ]
     [(s 'new-page)                  :new-page                  :get    "/pages/new"                       ]
     [(s 'show-page)                 :show-page                 :get    "/pages/:permalink"                ]
     [(s 'show-page-versions)        :show-page-versions        :get    "/pages/:permalink/versions"       ]
     [(s 'show-page-versions-atom)   :show-page-versions-atom   :get    "/pages/:permalink/versions.atom"  ]
     [(s 'show-page-version)         :show-page-version         :get    "/pages/:permalink/versions/:vid"  ]
     [(s 'edit-page)                 :edit-page                 :get    "/pages/:permalink/edit"           ]
     [(s 'create-page)               :create-page               :put    "/pages"                           ]
     [(s 'update-page)               :update-page               :post   "/pages/:permalink"                ]
     [(s 'destroy-page)              :destroy-page              :delete "/pages/:permalink"                ]
     [(s 'not-found)                 :not-found                 :any    "/:path" {:path ".*"}              ]]))
