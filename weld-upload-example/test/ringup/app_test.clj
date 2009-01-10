(ns ringup.app-test
  (:use clj-unit.core ringup.app ring.test-helpers)
  (:require [clj-file-utils.core :as file-utils]))

(def upload-info
  [(file-utils/file "test" "ringup" "assets" "test.png")
   "image/png"
   "test_filename.png"])

(deftest "create"
  (let [[status headers body :as response]
         (request app (path-info :create)
           {:params {:upload (upload upload-info)}})]
    (assert-redirect (path :index) response)))