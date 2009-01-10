(ns weld.controller-test
  (:use clj-unit.core clj-scrape.core (weld controller test-helpers)))

(deftest "respond"
  (assert=
    {:status 200 :headers {"Content-Type" "text/html"} :body "hello"}
    (respond "hello")))

(deftest "respond-404"
  (assert= {:status 404 :headers {"Content-Type" "text/html"} :body "hello"}
    (respond-404 "hello")))

(deftest "respond-500"
  (assert= {:status 500 :headers {"Content-Type" "text/html"} :body "o no"}
    (respond-500 "o no")))

(def base-redirect
  {:status  302
   :headers {"Location" "http://google.com"}
   :body    "You are being <a href=\"http://google.com\">redirected</a>."})

(deftest "redirect"
  (assert= base-redirect (redirect "http://google.com"))
  (assert= (assoc base-redirect :status 301)
    (redirect "http://google.com" {:status 301})))

(def base-file  (java.io.File. "/foo/bar.txt"))

(def base-send-file
  {:status  200
   :headers {"Content-Transfer-Encoding" "binary"
             "Content-Disposition"       "attachment; filename=bar.txt"}
   :body    base-file})

(deftest "send-file"
  (assert= base-send-file (send-file base-file))
  (assert=
    (assoc-in base-send-file [:headers "Content-Disposition"]
      "attachment; filename=custom.txt")
    (send-file base-file {:filename "custom.txt"})))