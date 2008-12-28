(ns ring.controller-test
  (:use clj-unit.core ring.controller clj-scrape.core))

(deftest "respond"
  (assert=
    [200 {"Content-Type" "text/html"} "hello"]
    (respond "hello")))

(deftest "respond-404"
  (assert= [404 {"Content-Type" "text/html"} "hello"]
    (respond-404 "hello")))

(deftest "respond-500"
  (assert= [500 {"Content-Type" "text/html"} "o no"]
    (respond-500 "o no")))

(def base-redirect
  [302
   {"Location" "http://google.com"}
  "You are being <a href=\"http://google.com\">redirected</a>."])

(deftest "redirect"
  (assert= base-redirect (redirect "http://google.com"))
  (assert= (assoc base-redirect 0 301)
    (redirect "http://google.com" {:status 301})))

(def base-file   (java.io.File. "/foo/bar.txt"))

(def base-send-file
  [200
   {"Content-Transfer-Encoding" "binary"
    "Content-Disposition"       "attachment; filename=bar.txt"}
   base-file])

(deftest "send-file"
  (assert= base-send-file (send-file base-file))
  (assert=
    (assoc-in base-send-file [1 "Content-Disposition"]
      "attachment; filename=custom.txt")
    (send-file base-file {:filename "custom.txt"})))