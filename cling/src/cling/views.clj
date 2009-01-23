(ns cling.views
  (:use
    (weld routing)
    (clj-html core utils helpers helpers-ext)
    (stash [core :only (errors)]
           [pagination :except (paginate)])
    (cling view-helpers))
  (:require
    (clj-time [core :as time])
    (stash    [core :as stash])))

;; Helpers
(defmacro layout
  [& body]
  `(html
     (doctype :xhtml-transitional)
     [:html {:xmlns "http://www.w3.org/1999/xhtml"}
       [:head
         [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
         [:title "Cling: A Clojure Wiki"]
         [:body
           [:div#container
             [:div#content
               ~@body]]]]]))

(defhtml error-messages-page
  [page]
  (when-let-html [errs (errors page)]
    [:div.error-messages
      [:h3 "There were problems with your submission:"]
      (for-html [err errs]
        [:p (name (:on err))])]))

(defhtml partial-page-form
  [page]
  [:p "title:"]
  [:p (text-field-tag "page[title]" (:title page))]
  [:p "body:"]
  [:p (text-area-tag  "page[body]"  (:body  page) {:rows 20 :cols 80})]
  [:p (submit-tag "Submit Page")])

;; Main Views
(defhtml index-pages
  [pages]
  (layout
    [:p (link-to "New Page" (path :new-page))])
    [:h1 "Pages"]
    [:div#pages
      (for-html [page pages]
        [:p (link-to (h (:title page)) (path :show-page page))])])

(defhtml show-page
  [page]
  (layout
    [:div {:class (str "page_" (:id page))}
      [:h2 (link-to (h (:title page)) (path :show-page page))]
      [:div.page_body
        (textilize (:body page))]]
    [:p (link-to "All Pages" (path :index-pages))
        " | "
        (link-to "Edit Page" (path :edit-page page))]))

(defhtml new-page
  [page]
  (layout
    [:h1 "New Page"]
    (error-messages-page page)
    (form {:to (path-info :create-page)}
      (partial-page-form page))))

(def left-quotes "&#8220;")
(def right-quotes "&#8221;")

(defhtml edit-page
  [page]
  (layout
    [:h1 (str "Edit Page " left-quotes (h (:title page)) right-quotes)]
    (error-messages-page page)
    (form {:to (path-info :update-page page)}
      (partial-page-form page))))

(defhtml not-found
  []
  (layout
    [:h3 "We're sorry - we couln't find that."]
    [:p  "Please return to the " (link-to "Home Page" (path :home))]))

(defhtml internal-error
  []
  (layout
    [:h3 "We're sorry - something went wrong."]
    [:p  "We've been notified of the problem and are looking into it."]))