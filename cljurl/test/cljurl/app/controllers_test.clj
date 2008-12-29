(ns cljurl.app.controllers-test
  (require cljurl.config
           [stash.core :as stash])
  (use clj-unit.core clojure.contrib.except clj-scrape.core
       cljurl.routing cljurl.app
       (cljurl.app models controllers controllers-test-helper)))

(deftest "with-filters"
  (binding [cljurl.config/+handle-exceptions+ true]
    (let [[status _ body] (with-filters false (throwf "o noews"))]
      (assert-status 500 status)
      (assert-match #"something went wrong" body))))

(deftest "not-found"
  (let [[status _ body] (request app [:get "/foo/bar"])]
    (assert-status 404 status)
    (assert-match #"we could not find that" body)))

(deftest "not-found-api"
  (let [[status _ body] (request app [:get "/foo/bar.js"])]
    (assert-status 404 status)
    (assert-match #"404 Not Found" body)))

(deftest "find-shortening"
  (with-fixtures [fx]
    (assert= (fx :shortenings :1 :url)
      (:url (find-shortening (fx :shortenings :1 :slug))))))

(deftest "with-shortening: when found"
  (with-fixtures [fx]
    (assert= (fx :shortenings :1 :url)
      (with-shortening false [shortening (fx :shortenings :1 :slug)]
        (:url shortening)))))

(deftest "with-shortening: when not found"
  (let [[status _ _] (with-shortening false [_ "missing"])]
    (assert-status 404 status)))

(deftest "index"
  (with-fixtures [_]
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
  (let [response (request app (path-info :create) {:params valid-params})
        [status headers body] response
        shortening (stash/find-one +shortening+ {:order [:created_at :desc]})]
    (assert-redirect (path :show shortening) response)))

(def invalid-params
  {:shortening {:url "foo"}})

(deftest "create: invalid shortening"
  (let [[_ _ body] (request app (path-info :create) {:params invalid-params})]
    (assert-selector
      (desc :p (text-match? #"valid-url")) body)
    (assert-selector
      (desc :form (attr= :action "/") (attr= :method "post")) body)
    (assert-selector
      (desc :form :input (attr= :value "foo")) body)))

(deftest "show: found shortening"
  (with-fixtures [fx]
    (let [[_ _ body] (request app
            (path-info :show {:slug (fx :shortenings :1 :slug)}))]
      (assert-selector
        (desc :p (text-match? #"Url shortened")) body))))

(deftest "show: missing shortening"
  (let [[status _ _] (request app (path-info :show {:slug "missing"}))]
    (assert-status 404 status)))

(deftest "expand: found shortening, existing ip"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          ht (fx :hits :on-1)
          response (request app (path-info :expand sh) {:remote-addr (:ip ht)})]
    (assert-redirect (:url sh) response)
    (assert= (inc (:hit_count ht))
      (:hit_count (stash/find-one +hit+
        {:where [:id := (:id ht)]}))))))

(deftest "expand: found shortening, new ip"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          response (request app (path-info :expand sh) {:remote-addr "new"})
          ht (stash/find-one +hit+ {:where [:ip := "new"]})]
      (assert-redirect (:url sh) response)
      (assert= 1 (:hit_count ht)))))

(deftest "expand: missing shortening"
  (let [[status _ _] (request app (path-info :expand {:slug "missing"}))]
    (assert-status 404 status)))

(deftest "expand-api: found shortening"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          [status headers body] (request app (path-info :expand-api sh)
                                  {:remote-addr "new"})
          ht (stash/find-one +hit+ {:where [:ip := "new"]})]
      (assert-status 200 status)
      (assert-json {:url (:url sh)} body))))

(deftest "expand-api: missing shortening"
  (let [[status headers _] (request app (path-info :expand-api {:slug "missing"}))]
    (assert-status 404 status)
    (assert-content-type "text/javascript" headers)))
