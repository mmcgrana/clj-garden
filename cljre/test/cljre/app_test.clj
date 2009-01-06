(ns cljre.app-test
  (:use (clj-unit core)
        (ring test-helpers)
        (cljre app)))

(def app (build-app :test))

(deftest "with-layout"
  (let [body (with-layout "inner")]
    (assert-markup [:head :title #"cljre"] body)))

(deftest "index-view"
  (let [body (index-view)]
    (assert-markup [:div {:id "header"} :h1 #"a Clojure Regex Editor"] body)))

(deftest "match-data: syntax error"
  (assert=
    {:status "syntax-error" :message "Unclosed group near index 2\na(\n  ^"}
    (match-data "a(" "ab")))

(deftest "match-data: no match"
  (assert= {:status "no-match"} (match-data "a" "b")))

(deftest "match-data: match"
  (assert= {:status "match", :result "(\"foo\")"} (match-data "foo" "foobar")))

(deftest "index"
  (let [[status headers body] (request app (path-info :index))]
    (assert-status 200 status)
    (assert-match #"cljre" body)))

(deftest "match"
  (let [[status headers body] (request app (path-info :match)
                                {:params {:pattern "foo" :string "foobar"}})]
    (assert-status 200 status)
    (assert-content-type "text/javascript" headers)
    (assert-json {:status "match" :result "(\"foo\")"} body)))
