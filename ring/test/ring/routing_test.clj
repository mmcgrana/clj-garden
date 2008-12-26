(ns ring.routing-test
  (:use clj-unit.core ring.routing))

(def show (fn [req] req))

(def c 'ring.routing-test)

(def routes
  [[c 'index          :index     :get  "/"                  ]
   [c 'show           :show      :get  "/show/:slug"        ]
   [c 'page-not-found :not-found :any  "/:path" {:path ".*"}]])

(def router (compiled-router routes "host"))

(deftest "recognize"
  (let [[action-fn params] (recognize router :get "/show/foo")]
    (assert= :shown (action-fn))
    (assert= {:slug "foo"} params)))

(deftest "path-info"
  (assert= [:get "/show/foo" {:extra "bar"}]
    (path-info router :show {:slug "foo" :extra "bar"})))

(deftest "path"
  (assert= "/show/foo"
    (path router :show {:slug "foo" :extra "bar"})))

(deftest "url-info"
  (assert= [:get "host/show/foo" {:extra "bar"}]
    (url-info router :show {:slug "foo" :extra "bar"})))

(deftest "url"
  (assert= "host/show/foo"
    (url router :show {:slug "foo" :extra "bar"})))
