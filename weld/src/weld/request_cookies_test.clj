(in-ns 'weld.request-test)

(deftest "cookie-parse"
  (assert=
    {:foo "bar" :baz "bat" :whiz "bang"}
    (cookie-parse "foo=bar;baz=bat; whiz=bang")))

(deftest "cookies"
  (let [req {:headers {"cookie" "f%26o=b%26r; biz=bat"}}]
    (assert= {:f&o "b&r" :biz "bat"} (cookies req))
    (assert= "bat" (cookies req :biz))))

(deftest "cookie-str"
  (assert= "f%26o=b%26r" (cookie-str :f&o "b&r"))
  (assert=
    "f%26o=b%26r; domain=.test.net; path=/; expires=Thu, 01-Jan-1970 00:01:00 GMT"
    (cookie-str :f&o "b&r"
      {:domain ".test.net" :path "/" :expires (time/zero)})))

(deftest "with-cookie"
  (assert=
    [:s {"Set-Cookie" ["foo=bar"]} :b]
    (with-cookie :foo "bar" [:s {} :b]))
  (assert=
    [:s {"Set-Cookie" ["foo=bar" "biz=bat"]} :b]
    (with-cookie :biz "bat" [:s {"Set-Cookie" ["foo=bar"]} :b]))
  (assert=
    [:s {"Set-Cookie" ["foo=bar; path=/"]} :b]
    (with-cookie :foo "bar" {:path "/"} [:s {} :b])))

(deftest "less-cookie"
  (assert=
    [:s {"Set-Cookie" ["foo=; expires=Thu, 01-Jan-1970 00:01:00 GMT"]} :b]
    (less-cookie :foo [:s {} :b])))

