(ns clj-scrape.core-test
  (use clj-unit.core clj-scrape.core))

(defn asset-input-stream [name]
  (let [cl     (clojure.lang.RT/ROOT_CLASSLOADER)
        url    (.getResource cl name)
        path   (.getPath url)]
    (java.io.FileInputStream. path)))

(defn string-input-stream [string]
  (java.io.StringReader. string))

(defn asset-dom [name]
  (dom (asset-input-stream name)))

(defn string-dom [string]
  (dom (string-input-stream string)))

(def simple-dom
  (string-dom
    "<html><body><p id='a' custom='no' class='1'>one</p><p id='b' unique='yes' custom='yes' class='2'>two</p></body></html>"))

; (deftest "xml->"
;   (assert= '("one" "two")
;     (xml-> simple-dom :body :p text)))
; 
; (deftest "xml1->"
;   (assert= "one"
;     (xml1-> simple-dom :body :p text)))

(deftest "class="
  (assert= "two"
    (xml1-> simple-dom desc (class= "2") text)))

(deftest "id="
  (assert= "two"
    (xml1-> simple-dom desc (id= "b") text)))

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
