(ns clj-scrape.core-test
  (use clj-unit.core clj-scrape.core))

(defn string-dom [string]
  (dom (java.io.StringReader. string)))

(def simple-dom
  (string-dom
    "<html><body><p id='a' custom='no' class='1'>one</p><p id='b' unique='yes' custom='yes' class='2'>two</p></body></html>"))

(defn assert-xml1-> [expected & predicates]
  (assert= expected (apply xml1-> simple-dom predicates)))

(deftest "class="
  (assert-xml1-> "two" desc (class= "2") text))

(deftest "id="
  (assert-xml1-> "two" desc (id= "b") text))

(deftest "attrs="
  (assert-xml1-> "two" desc {:id "b" :unique "yes"} text))

(deftest "attr?"
  (assert-xml1-> "two" desc (attr? :unique) text))

(deftest "attr-match?"
  (assert-xml1-> "two" desc (attr-match? :custom #"ye.") text))

(deftest "nth-elem"
  (assert-xml1-> "two" desc :p (nth-elem 2) text))

(deftest "text-match?"
  (assert-xml1-> "two" desc :p (text-match? #"tw.") text))

(deftest "xml1->: :desc"
  (assert-xml1-> "two" :desc (class= 2) text))
