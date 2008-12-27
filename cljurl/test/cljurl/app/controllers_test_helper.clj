(ns cljurl.app.controllers-test-helper
  (require [stash.core :as stash])
  (use clj-unit.core cljurl.app.models clj-time.core clj-scrape.core))

(def shortening-map1 {:slug "short1" :url "http://google.com" :created_at (now)})
(def shortening-map2 {:slug "short2" :url "http://amazon.com" :created_at (now)})

(defmacro with-fixtures
  "TODOC"
  [& body]
  `(do
     (stash/delete-all +shortening+)
     (doseq [short# [shortening-map1 shortening-map2]]
       (stash/persist-insert (stash/init +shortening+ short#)))
     ~@body))

(defn request
  "Returns the response of app to mock request build according to the method,
  path and optional params."
  [app [method path] & [params]]
  (let [env {:uri path}
        env (assoc env :request-method method)
        env (assoc env :query-string   (serialize ))]
    (app env)))

(defmacro assert-status
  "TODOC"
  [expected-status actual-status-form]
  `(let [actual-status# ~actual-status-form]
     (assert-that (= ~expected-status actual-status#)
       (format "Expected status of %s, but got %s"
         ~expected-status actual-status#))))

(defmacro assert-selector
  "TODOC"
  [expected-selector actual-body]
  `(let [actual-dom# (dom (java.io.StringReader. ~actual-body))]
     (assert-that (xml1-> actual-dom# ~@expected-selector)
       (format "Expecting body matching %s, but did not.",
         '~expected-selector))))
