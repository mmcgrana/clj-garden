(ns stash.validators-test
  (:use clj-unit.core stash.core stash.validators))

(def url-validator (valid-url :url))

(deftest "valid-url: returns nil for valid urls"
  (doseq [url ["http://google.com" "http://google.com/foo?bar=bat"]]
    (assert-not (url-validator {:url url}))))

(deftest "valid-url: returns error for invalid url"
  (doseq [url ["fobar" "http://google"]]
    (assert= (struct +error+ :url :valid-url) (url-validator {:url url}))))
