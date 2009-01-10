(ns weldup.app-test
  (:use clj-unit.core weldup.app weld.test-helpers)
  (:require [clj-file-utils.core :as file-utils]))

(def mock-upload
  (upload
    (file-utils/file "test" "ringup" "assets" "test.png")
    "image/png"
    "test_filename.png"))

(deftest "create"
  (let [{:keys [status headers body] :as response}
         (request app (path-info :create)
           {:params {:upload mock-upload}})]
    (assert-redirect (path :index) response)))