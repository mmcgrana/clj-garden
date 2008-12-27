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
  (with-fixtures
    (assert= (:url shortening-map1)
      (:url (find-shortening {:slug "short1"})))))

(deftest "with-shortening: when found"
  (with-fixtures
    (assert= (:url shortening-map1)
      (with-shortening [shortening {:slug "short1"}]
        (:url shortening)))))

(deftest "with-shortening: when not found"
  (let [[status _ _] (with-shortening [_ {:slug "missing"}])]
    (assert-status 404 status)))

(deftest "index"
  (with-fixtures
    (let [[status _ body] (request app (path-info :index))]
      (assert-status 200 status)
      (assert-selector
        (desc :a (attr= :href "/new") (text= "new shortening")) body)
      (assert-selector
        (desc :h3 (text= "Recent Shortenings")) body)
      (assert-selector
        (desc :p (text-match? #" => ")) body))))

(deftest "new"
  (with-fixtures
    (let [[status _ body] (request app (path-info :new))]
      (assert-status 200 status)
      (assert-selector
        (desc :form :p (text-match? #"Enter url:")) body)
      (assert-selector
        (desc :form (attr= :action "/") (attr= :method "post")) body)
      (assert-selector
        (desc :form :input (attr= :type "text") (attr= :name "shortening[url]"))
        body))))

(def valid-params
  {:shortening {:url "http://amazon.com"}})

(deftest "create: valid shortening"
  (with-fixtures
    (let [response (request app (path-info :create) valid-params)
          [status headers body] response
          shortening (stash/find-one +shortening+ {:order [:created_at :desc]})]
      (assert-redirect (path :show shortening) response))))

(def invalid-params
  {:shortening {:url "foo"}})

(deftest "create: invalid shortening"
  (with-fixtures
    (let [[_ _ body] (request app (path-info :create) invalid-params)]
      (assert-selector
        (desc :p (text-match? #"valid-url")) body)
      (assert-selector
        (desc :form (attr= :action "/") (attr= :method "post")) body)
      (assert-selector
        (desc :form :input (attr= :value "foo")) body))))

(deftest "show: found shortening"
  (with-fixtures
    (let [[_ _ body] (request app (path-info :create))])))

(deftest "show: missing shortening")

(deftest "expand: found shortening")

(deftest "expand: missing shortening")