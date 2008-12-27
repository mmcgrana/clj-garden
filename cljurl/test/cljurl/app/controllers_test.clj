(ns cljurl.app.controllers-test
  (require cljurl.config)
  (use clj-unit.core clojure.contrib.except
       cljurl.routing cljurl.app
       (cljurl.app models controllers controllers-test-helper)))

(deftest "with-filters"
  (binding [cljurl.config/+env+ :prod]
    (let [[status _ body] (with-filters (throwf "o noews"))]
      (assert-status 500 status)
      (assert-match #"something went wrong" body))))

(deftest "page-not-found"
  (let [[status _ body] (request app "/foo/bar")]
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
    (let [[status _ body] (request app (path :index))]
      (assert-status 200 status)
      ; new shortening link
      (assert-selector body)
      ; recent shortening header
      (assert-selector body)
      ; shortening listing
      (assert-selector body)
      )))

(deftest "new"
  (with-fixtures
    (let [[status _ body] (request app (path :new))]
      (assert-status 200 status)
      ; enter url
      (assert-selector body)
      ; form
      (assert-selector body)
      ; url field
      (assert-selecor body)
      )))

