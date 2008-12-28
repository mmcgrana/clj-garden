(ns cljurl.app.controllers-test-helper
  (require [stash.core      :as stash]
           [ring.http-utils :as http-utils])
  (use clj-unit.core cljurl.app.models clj-time.core clj-scrape.core))

(def shortening-map1 {:slug "short1" :url "http://google.com" :hit_count 0 :created_at (now)})
(def shortening-map2 {:slug "short2" :url "http://amazon.com" :hit_count 0 :created_at (now)})

(defmacro with-fixtures
  "TODOC"
  [[binding-sym] & body]
  `(do
     (stash/delete-all +shortening+)
     (let [s1# (stash/persist-insert (stash/init* +shortening+ shortening-map1))
           s2# (stash/persist-insert (stash/init* +shortening+ shortening-map2))
           ~binding-sym {:shortenings {:1 s1# :2 s2#}}]
       ~@body)))

(defn request
  "Returns the response of app to mock request build according to the method,
  path and optional params."
  [app [method path] & [params]]
  (app {:uri path
        :request-method method
        :query-string (http-utils/query-unparse (or params {}))}))

(defmacro assert-status
  "TODOC"
  [expected-status actual-status-form]
  `(let [actual-status# ~actual-status-form]
     (assert-that (= ~expected-status actual-status#)
       (format "Expected status of %s, but got %s"
         ~expected-status actual-status#))))

(defmacro assert-redirect
  "TODOC"
  [expected-path response-form]
  `(let [[status# headers# body#] ~response-form
         location#                (get headers# "Location")]
     (assert-that (and (and (>= status# 300) (< status# 400))
                       (= ~expected-path location#))
       (format "Expecting redirect status and Location of %s, but got %s and %s."
         ~expected-path status# location#))))

(defmacro assert-selector
  "TODOC"
  [expected-selector actual-body]
  `(let [actual-dom# (dom (java.io.StringReader. ~actual-body))]
     (assert-that (xml1-> actual-dom# ~@expected-selector)
       (format "Expecting body matching %s, but did not.",
         '~expected-selector))))
