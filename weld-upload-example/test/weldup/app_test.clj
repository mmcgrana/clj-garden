(ns weldup.app-test
  (:use clj-unit.core weldup.app clj-file-utils.core
        (weld routing test-helpers))
  (:require [stash.core :as stash]))

(defmacro deftest-db
  [description body]
  `(deftest ~description
     (stash/delete-all +upload+)
     ~body
     (stash/delete-all +upload+)))

(def mock-upload
  (upload
    (file "test" "weldup" "assets" "test.png")
    "image/png"
    "test_filename.png"))

(deftest-db "create"
  (let [{:keys [status headers body] :as response}
         (request app (path-info :create) {:params {:upload mock-upload}})]
    (assert-redirect (path :index) response)
    (assert= 1 (stash/count-all +upload+))
    (let [upload (stash/find-one +upload+)]
      (assert= "test_filename.png" (:filename     upload))
      (assert= "image/png"         (:content_type upload))
      (assert= 218465              (:size         upload))
      (let [f (upload-file upload)]
        (assert-that (.exists f))))))