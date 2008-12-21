(ns cljurl.routing-test
  (:require cljurl.app.controllers cljurl.config)
  (:use clj-unit.core cljurl.routing))

(deftest "action-recognizer"
  (assert= [(ns-resolve 'cljurl.app.controllers 'show) {:slug "foo"}]
    (action-recognizer :get "/show/foo")))

(deftest "path-info"
  (assert= [:get "/show/foo" {:extra "bar"}]
    (path-info :show {:slug "foo" :extra "bar"})))

(deftest "path"
  (assert= "/show/foo" (path :show {:slug "foo" :extra "bar"})))

(deftest "url-info"
  (binding [cljurl.config/*app-host* "host"]
    (assert= [:get "host/show/foo" {:extra "bar"}]
      (url-info :show {:slug "foo" :extra "bar"}))))

(deftest "url"
  (binding [cljurl.config/*app-host* "host"]
    (assert= "host/show/foo" (url :show {:slug "foo" :extra "bar"}))))