(ns cljurl.app.view-helpers-test
  (:use clj-unit.core cljurl.app.view-helpers))

(deftest "hstr"
  (assert= ":foo&amp;" (hstr :foo&)))

(deftest "browser-method?"
  (assert-fn     browser-method? :get)
  (assert-fn     browser-method? :post)
  (assert-not-fn browser-method? :put)
  (assert-not-fn browser-method? :delete))

(deftest "method-str"
  (assert= "put" (method-str :put)))

(deftest "text-field-tag"
  (assert= "<input type=\"text\" name=\"foo[bar]\" value=\"3\" />"
    (text-field-tag "foo[bar]" 3)))

(deftest "hidden-field-tag"
  (assert= "<input type=\"hidden\" name=\"foo[bar]\" value=\"3\" />"
    (hidden-field-tag "foo[bar]" 3)))

(deftest "submit-tag"
  (assert= "<input name=\"commit\" type=\"submit\" value=\"foo\" />"
    (submit-tag "foo")))

(deftest "form-to"
  (assert= "<form action=\"/foo\" method=\"get\">inner</form>"
    (form-to [:get "/foo"] "inner"))
  (assert= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"put\" />inner</form>"
    (form-to [:put "/foo"] "inner")))

(deftest "link-tag"
  (assert= "<a href=\"http://google.com\">foo</a>"
    (link-tag "foo" "http://google.com")))