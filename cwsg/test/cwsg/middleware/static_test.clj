(ns cwsg.middleware.static-test
  (:use (clj-unit.core))
  (:require (cwsg.middleware.static))
  (:import (Java.io File)))


(deftest "from-dir throws on initialization when directory missing"
  (assert-throws
    (cwsg.middleware.static/from-dir (File. "does/not/exist"))))


(defvar- static-app
  (cwsg.middleware.static/from-dir (File. "does/not/matter")))

(deftest "from-dir app returns a 304 if \"..\" is used in the path"
  (assert= 304
    (first (static-app {:request-method "GET" :uri "/../../bar"}))))

(deftest "from-dir app returns a 404 if the request is not GET or HEAD"
  (assert= 404
    (first (static-app {:request-method "POST" :uri "/foo/bar"}))))


(deftest "from-dir app returns a 404 if the file does not exists")

(deftest "from-dir app returns a 404 if the file is not readable")

(deftest "from-dir app returns a file response for readable files")

(deftest "from-dir app returns a file response for readable index files")

(deftest "from-dir app returns a file respose for readable implied html files")





