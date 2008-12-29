(ns cljurl.test-helpers
  (require [stash.core         :as stash]
           [ring.http-utils    :as http-utils]
           [org.danlarkin.json :as json])
  (use clj-unit.core cljurl.models clj-time.core clj-scrape.core
       [cljurl.view-helpers :only (str-json)]))

(def shortening-map1 {:slug "short1" :url "http://google.com" :created_at (now)})
(def shortening-map2 {:slug "short2" :url "http://amazon.com" :created_at (now)})
(def hit-map1        {:ip "auserip" :created_at (now) :updated_at (now) :hit_count 3})

(defmacro with-fixtures
  "Populates the db with fixtures, binds binding-sym to a fn that can be used
  to access the original versions of the model."
  [[binding-sym] & body]
  `(do
     (stash/delete-all +shortening+)
     (let [s1# (stash/persist-insert (stash/init* +shortening+ shortening-map1))
           s2# (stash/persist-insert (stash/init* +shortening+ shortening-map2))
           h1# (stash/persist-insert (stash/init* +hit+ (assoc hit-map1 :shortening_id (:id s1#))))
           fixtures# {:shortenings {:1 s1# :2 s2#} :hits {:on-1 h1#}}
           ~binding-sym (fn [& path#] (get-in fixtures# path#))]
       ~@body)))