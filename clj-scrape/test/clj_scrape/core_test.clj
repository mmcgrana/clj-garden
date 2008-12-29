(ns clj-scrape.core-test
  (use clj-unit.core clj-scrape.core))

(defn string-dom [string]
  (dom (java.io.StringReader. string)))

(def simple-dom
  (string-dom
    "<html><body><p id='a' custom='no' class='1'>one</p><p id='b' unique='yes' custom='yes' class='2'>two</p></body></html>"))

(deftest "class="
  (assert= "two"
    (xml1-> simple-dom desc (class= "2") text)))

(deftest "id="
  (assert= "two"
    (xml1-> simple-dom desc (id= "b") text)))

(deftest "attrs="
  (assert= "two"
    (xml1-> simple-dom desc {:id "b" :unique "yes"} text)))

(deftest "attr?"
  (assert= "two"
    (xml1-> simple-dom desc (attr? :unique) text)))

(deftest "attr-match?"
  (assert= "two"
    (xml1-> simple-dom desc (attr-match? :custom #"ye.") text)))

(deftest "nth-elem"
  (assert= "two"
    (xml1-> simple-dom desc :p (nth-elem 2) text)))

(deftest "text-match?"
  (assert= "two"
    (xml1-> simple-dom desc :p (text-match? #"tw.") text)))
