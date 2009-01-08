(ns cljurl.requests-test
  (require [stash.core :as stash]
           cljurl.app)
  (use clj-unit.core clj-scrape.core
       (cljurl routing models controllers test-helpers)
       ring.test-helpers))

(def app cljurl.app/app)

(deftest "with-filters"
  (binding [cljurl.config/handle-exceptions? true]
    (let [[status _ body] (with-filters false (throw (Exception. "o noews")))]
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

(deftest "with-shortening: when found"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)]
      (assert= (:url sh)
        (with-shortening false [shortening (:slug sh)]
          (:url shortening))))))

(deftest "with-shortening: when not found"
  (let [[status _ _] (with-shortening false [_ "missing"])]
    (assert-status 404 status)))

(deftest "index"
  (with-fixtures [_]
    (let [[status _ body] (request app (path-info :index))]
      (assert-status 200 status)
      (assert-markup [:a {:href "/new"} "new shortening"] body)
      (assert-markup [:h3 "Recent Shortenings"] body)
      (assert-markup [:p #" => "] body))))

(deftest "new"
  (let [[status _ body] (request app (path-info :new))]
    (assert-status 200 status)
    (assert-markup [:form :p #"Enter url:"] body)
    (assert-markup [:form {:action "/" :method "post"}] body)
    (assert-markup [:form :input {:type "text" :name "shortening[url]"}] body)))

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
    (assert-markup [:p #"valid-url"] body)
    (assert-markup [:form {:action "/" :method "post"}] body)
    (assert-markup [:form :input {:name "shortening[url]" :value "foo"}] body)))

(deftest "show: found shortening"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          [_ _ body] (request app (path-info :show sh))]
      (assert-markup [:p #"Url shortened"] body))))

(deftest "show: missing shortening"
  (let [[status _ _] (request app (path-info :show {:slug "missing"}))]
    (assert-status 404 status)))

(deftest "expand: found shortening, existing ip"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          ht (fx :hits :on-1)
          response (request app (path-info :expand sh) {:remote-addr (:ip ht)})]
      (assert-redirect (:url sh) response)
      (assert= (inc (:hit_count ht)) (:hit_count (stash/reload ht))))))

(deftest "expand: missing shortening"
  (let [[status _ _] (request app (path-info :expand {:slug "missing"}))]
    (assert-status 404 status)))

(deftest "expand-api: found shortening"
  (with-fixtures [fx]
    (let [sh (fx :shortenings :1)
          [status headers body] (request app (path-info :expand-api sh))]
      (assert-status 200 status)
      (assert-json {:url (:url sh)} body))))

(deftest "expand-api: missing shortening"
  (let [[status headers _] (request app (path-info :expand-api {:slug "missing"}))]
    (assert-status 404 status)
    (assert-content-type "text/javascript" headers)))
