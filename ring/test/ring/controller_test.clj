(ns ring.controller-test
  (:use clj-unit.core ring.controller))

(deftest "not-found"
  (assert= [404 {"Content-Type" "text/html"} "hello"]
    (not-found "hello")))

(deftest "internal-error"
  (assert= [500 {"Content-Type" "text/html"} "o no"]
    (internal-error "o no")))

(deftest "render"
  (assert=
    [200 {"Content-Type" "text/html"} "hello"]
    (render "hello")))

(def base-redirect
  [302 {"Location" "http://google.com"}
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