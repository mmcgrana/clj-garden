(ns gitcred.data-test
  (:use clj-unit.core gitcred.data clj-time.core stash.core)
  (:import (org.apache.commons.io IOUtils)))

; TODO: need separate test db

(defmacro with-clean-db
  [& body]
  `(try
     (delete-all +user+)
     (delete-all +follow+)
     ~@body
    (finally
      (delete-all +user+)
      (delete-all +follow+))))

(defn asset-input-stream [name]
  (let [cl     (clojure.lang.RT/ROOT_CLASSLOADER)
        url    (.getResource cl name)]
    (if url
      (java.io.FileInputStream. (.getPath url))
      (throw (Exception. (str "No file at " name))))))

(defn +mmcgrana-page+ []
  (asset-input-stream "gitcred/files/mmcgrana.html"))

(def +mmcgrana-followers+
  ["adamwiggins" "defunkt" "technoweenie" "pjhyett" "topfunky" "timburks"
   "schacon" "wycats" "michaelklishin" "technomancy" "haikuwebdev" "joe"
   "robolson" "northern-bites" "mlins" "weavejester" "trhermans" "jstrom"
   "hwork" "techcrunch" "ppierre" "Chouser" "rosado" "stuarthalloway"
   "hiredman"])

(deftest "user-url"
  (assert= "http://github.com/mmcgrana"
    (user-url {:username "mmcgrana"})))

(deftest "parse-usernames"
  (assert= +mmcgrana-followers+
    (parse-usernames (+mmcgrana-page+))))

; (prn (scrape-usernames {:username "mmcgrana"}))

(deftest "ensure-usernames"
  (with-clean-db
    (create* +user+ {:username "foo" :discovered_at (now)})
    (ensure-usernames ["foo" "bar"])
    (assert-that (exist? +user+ {:where [:username := "bar"]}))))

(deftest "allign-follows"
  (with-clean-db
    (create* +follow+ {:from_username "mmcgrana" :to_username "foo"})
    (allign-follows {:username "mmcgrana"} ["foo" "bat"])
    (assert-that (exist? +follow+
                   {:where [:and [:from_username := "mmcgrana"]
                                 [:to_username := "foo"]]}))))

;(deftest "last-scraped-at")
;(deftest "keep-scraping?")
;(deftest "next-user")
;(deftest "scrape1")
;(deftest "scrape")
;(deftest "ensure-seed-user")
;(deftest "run")