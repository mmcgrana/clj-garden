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
     [(s 'search-pages)              :search-pages              :get    "/pages/search"                    ]
     [(s 'new-page)                  :new-page                  :get    "/pages/new"                       ]
     [(s 'show-page)                 :show-page                 :get    "/pages/:slug"                ]
     [(s 'show-page-versions)        :show-page-versions        :get    "/pages/:slug/versions"       ]
     [(s 'show-page-versions-atom)   :show-page-versions-atom   :get    "/pages/:slug/versions.atom"  ]
     [(s 'show-page-version)         :show-page-version         :get    "/pages/:slug/versions/:vid"  ]
     [(s 'show-page-diff)            :show-page-diff            :get    "/pages/:slug/diff"           ]
     [(s 'edit-page)                 :edit-page                 :get    "/pages/:slug/edit"           ]
     [(s 'create-page)               :create-page               :put    "/pages"                           ]
     [(s 'update-page)               :update-page               :post   "/pages/:slug"                ]
     [(s 'destroy-page)              :destroy-page              :delete "/pages/:slug"                ]
     [(s 'not-found)                 :not-found                 :any    "/:path" {:path ".*"}              ]]))
