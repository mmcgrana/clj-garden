(ns ring.http-utils-test
  (:use clj-unit.core
        ring.http-utils))

(deftest "url-escape, url-unescape: round-trip as expected"
  (let [given "foo123!@#$%^&*(){}[]<>?/"]
    (assert= given (url-unescape (url-escape given)))))

(deftest "parse-pairs")

; Test cases mostly from Merb.
(def query-parse-cases
  [[""                                    {}]
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
   ["foo[bar][][baz]=1&foo[bar][][zot]=2&foo[bar][][fuz]=A&foo[bar][][baz]=3&foo[bar][][zot]=4" {:foo {:bar [{:baz "1" :zot "2" :fuz "A"} {:baz "3" :zot "4"}]}}]])

(doseq [[query-string query-params] query-parse-cases]
  (deftest (format "query-parse: works on %s" query-string)
    (assert= query-params (query-parse query-string))))

(doseq [query-params (map second query-parse-cases)]
  (deftest (format "query-unparse: works on %s" query-params)
    (assert= query-params (query-parse (query-unparse query-params)))))

(deftest "cookie-parse"
  (assert=
    {:foo "bar" :baz "bat" :whiz "bang"}
    (cookie-parse "foo=bar;baz=bat; whiz=bang")))

; -
; [:foo "bar"] [:baz "bat"]
; [:foo [] "bar"] [:foo [] "baz"]
; [:foo [] :bar "1"] [:foo [] :bar 2]
; [:foo :bar [] :baz "1"] [:foo :bar [] :baz "2"]
; [:foo :1 "bar"]
; [:foo :2 "bar"]
; [:foo :bar :baz "1"] [:foo :bar :zot "2"] [:foo :bar :zip "3"] [:foo :bar :buz "4"]

;(defn query-unparse-nested
;  [front tail]
;  (cond
;    (not (coll? tail))
;      (str front "=" (url-escape (str tail)))
;    (map? tail)
;      ()
;    :else
;      (str-join "&"
;        (map (fn [inner] (query-unparse-nested (str front "[]") inner))
;             tail)))

;(defn query-unparse-lines-nested
;  [tail]
;  (cond
;    (= (count tail) 1)
;      (str "=" (url-escape (str (first tail))))
;    (= [] (first tail))
;      (str "")

; (defn query-unparse
;   "Returns a query string corresponding to the params."
;   [params]
;   (str-join "&"
;     (map (fn [[k v]] (str (url-escape (name key)) (query-unparse-nested v)))
;          params)))

;(def cases
;  {:foo "b&r" :biz "bat"}
;  {:foo 2     :biz 7}
;  {:foo ["b&r" "bat"] :cat "hat"}
;  {:foo [2 7] :cat 3}
;  {:foo {:biz "b&t"} :cat "hat"}
;  {:foo {:bar 2} :cat 3}])

;(deftest "qeuery-unparse"
;  (doseq [case cases]
;    (assert= case (query-parse (query-unparse case)))))
