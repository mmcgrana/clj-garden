(ns stash.validators-test
  (:use clj-unit.core stash.core stash.validators))

(deftest "valid-url"
  (let [url-validator (valid-url :url)]
    (doseq [url ["http://google.com" "http://google.com/foo?bar=bat"]]
      (assert-not (url-validator {:url url})))
      (doseq [url ["fobar" "http://google"]]
        (assert= (error :url :valid-url) (url-validator {:url url})))))

(deftest "presence: nil attribute"
  (let [presence-validator (presence :url)]
    (assert-not (presence-validator {:url "not blank"}))
    (assert= (error :url :presence) (presence-validator {:url nil}))))
