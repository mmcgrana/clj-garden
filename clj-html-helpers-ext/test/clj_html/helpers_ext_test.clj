(ns clj-html.helpers-ext-test
  (:use clj-unit.core clj-html.helpers-ext))

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

(deftest "file-field-tag"
  (assert= "<input type=\"file\" name=\"foo[bar]\" />"
    (file-field-tag "foo[bar]")))

(deftest "submit-tag"
  (assert= "<input name=\"commit\" type=\"submit\" value=\"foo\" />"
    (submit-tag "foo")))

(deftest "form-to"
  (assert= "<form action=\"/foo\" method=\"get\">inner</form>"
    (form-to [:get "/foo"] "inner"))
  (assert= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"put\" />inner</form>"
    (form-to [:put "/foo"] "inner")))

(deftest "link-to"
  (assert= "<a href=\"http://google.com\">foo</a>"
    (link-to "foo" "http://google.com")))

(deftest "delete-button"
  (assert= "<form method=\"post\" action=\"/foo\"><input type=\"hidden\" name=\"_method\" value=\"delete\" /><input name=\"commit\" type=\"submit\" value=\"Delete\" /></form>"
    (delete-button "Delete" "/foo")))