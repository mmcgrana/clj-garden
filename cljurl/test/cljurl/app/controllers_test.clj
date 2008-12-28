(ns cljurl.app.controllers-test
  (require cljurl.config
           [stash.core :as stash])
  (use clj-unit.core clojure.contrib.except clj-scrape.core
       cljurl.routing cljurl.app
       (cljurl.app models controllers controllers-test-helper)))

(deftest "with-filters"
  (binding [cljurl.config/+env+ :prod]
    (let [[status _ body] (with-filters (throwf "o noews"))]
      (assert-status 500 status)
      (assert-match #"something went wrong" body))))

(deftest "page-not-found"
  (let [[status _ body] (request app [:get "/foo/bar"])]
    (assert-status 404 status)
    (assert-match #"we could not find that" body)))

(deftest "find-shortening"
  (with-fixtures [fx]
    (assert= (get-in fx [:shortenings :1 :url])
      (:url (find-shortening (get-in fx [:shortenings :1 :slug]))))))

(deftest "with-shortening: when found"
  (with-fixtures [fx]
    (assert= (get-in fx [:shortenings :1 :url])
      (with-shortening [shortening (get-in fx [:shortenings :1 :slug])]
        (:url shortening)))))

(deftest "with-shortening: when not found"
  (let [[status _ _] (with-shortening [_ "missing"])]
    (assert-status 404 status)))

(deftest "index"
  (with-fixtures [fx]
    (let [[status _ body] (request app (path-info :index))]
      (assert-status 200 status)
      (assert-selector
        (desc :a (attr= :href "/new") (text= "new shortening")) body)
      (assert-selector
        (desc :h3 (text= "Recent Shortenings")) body)
      (assert-selector
        (desc :p (text-match? #" => ")) body))))

(deftest "new"
  (let [[status _ body] (request app (path-info :new))]
    (assert-status 200 status)
    (assert-selector
      (desc :form :p (text-match? #"Enter url:")) body)
    (assert-selector
      (desc :form (attr= :action "/") (attr= :method "post")) body)
    (assert-selector
      (desc :form :input (attr= :type "text") (attr= :name "shortening[url]"))
      body)))

(def valid-params
  {:shortening {:url "http://amazon.com"}})

(deftest "create: valid shortening"
  (let [response (request app (path-info :create) valid-params)
        [status headers body] response
        shortening (stash/find-one +shortening+ {:order [:created_at :desc]})]
    (assert-redirect (path :show shortening) response)))

(def invalid-params
  {:shortening {:url "foo"}})

(deftest "create: invalid shortening"
  (let [[_ _ body] (request app (path-info :create) invalid-params)]
    (assert-selector
      (desc :p (text-match? #"valid-url")) body)
    (assert-selector
      (desc :form (attr= :action "/") (attr= :method "post")) body)
    (assert-selector
      (desc :form :input (attr= :value "foo")) body)))

(deftest "show: found shortening"
  (with-fixtures [fx]
    (let [[_ _ body] (request app
            (path-info :show {:slug (get-in fx [:shortenings :1 :slug])}))]
      (assert-selector
        (desc :p (text-match? #"Url shortened")) body))))

(deftest "show: missing shortening"
  (let [[status _ _] (request app (path-info :show {:slug "missing"}))]
    (assert-status 404 status)))

(deftest "expand: found shortening"
  (with-fixtures [fx]
    (let [shortening (get-in fx [:shortenings :1])
          response (request app (path-info :expand shortening))]
    (assert-redirect (:url shortening) response)
    (assert= (inc (:hit_count shortening))
      (:hit_count (stash/find-one +shortening+
                    {:where [:slug := (:slug shortening)]}))))))

(deftest "expand: missing shortening"
  (let [[status _ _] (request app (path-info :expand {:slug "missing"}))]
    (assert-status 404 status)))