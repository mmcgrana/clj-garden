(ns ring.http-util-test
  (:use clj-unit.core
        rint.http-util
        [clojure.contrib.def :only (defvar-)]))

(deftest "url-escape and url-unescape round-trips as expected"
  (let [given "foo123!@#$%^&*(){}[]<>?/"]
    (assert= given (url-unescape (url-escape given)))))

(defvar- query-parse-cases
  [[nil                                   {}]
   ["foo=bar&baz=bat"                     {:foo "bar", :baz "bat"}]
   ["foo=bar&foo=baz"                     {:foo "baz"}]
   ["foo[]=bar&foo[]=baz"                 {:foo ["bar" "baz"]}]
   ["foo[][bar]=1&foo[][bar]=2"           {:foo [{:bar "1"} {:bar "2"}]}]
   ["foo[bar][][baz]=1&foo[bar][][baz]=2" {:foo {:bar [{:baz "1"} {:baz "2"}]}}]
   ["foo[1]=bar&foo[2]=baz"               {:foo {:1 "bar" :2 "baz"}}]
   ["foo[bar][baz]=1&foo[bar][zot]=2&foo[bar][zip]=3&foo[bar][buz]=4" {:foo {:bar {:baz "1" :zot "2" :zip "3" :buz "4"}}}]
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][zip]=3&foo[bar][][buz]=4" {:foo {:bar [{:baz "1" :zot "2" :zip "3" :buz "4"}]}}]
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][baz]=3&foo[bar][][zot]=4" {:foo {:bar [{:baz "1" :zot "2"} {:baz "3" :zot "4"}]}}]
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][fuz]=A&foo[bar][][baz]=3&foo[bar][][zot]=4&foo[bar][][fuz]=B" {:foo {:bar [{:baz "1" :zot "2" :fuz "A"} {:baz "3" :zot "4" :fuz "B"}]}}]
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][fuz]=A&foo[bar][][baz]=3&foo[bar][][zot]=4&foo[bar][][foz]=C" {:foo {:bar [{:baz "1" :zot "2" :fuz "A"} {:baz "3" :zot "4" :foz "C"}]}}]
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][fuz]=A&foo[bar][][baz]=3&foo[bar][][zot]=4" {:foo {:bar [{:baz "1" :zot "2" :fuz "A"} {:baz "3" :zot "4"}]}}]
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][fuz]=A&foo[bar][][baz]=3&foo[bar][][zot]=4&foo[bar][][fuz]=B&foo[bar][][foz]=C" {:foo {:bar [{:baz "1" :zot "2" :fuz "A"} {:baz "3" :zot "4" :fuz "B" :foz "C"}]}}]])


(doseq [[query-string query-params] query-parse-cases]
  (deftest (format "query-parse works" query-string)
    (assert= query-params (query-parse query-string))))