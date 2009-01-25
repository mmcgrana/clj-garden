(ns cling.views
  (:use
    (weld routing)
    (clj-html core utils helpers helpers-ext)
    (stash [core :only (errors new?)]
           [pagination :except (paginate)])
    (cling view-helpers utils))
  (:require
    (clj-time [core :as time])
    (stash    [core :as stash])))

;; Helpers
(defmacro layout
  [assigns-form & body]
  `(let [assigns# ~assigns-form]
     (html
       (doctype :xhtml-transitional)
       [:html {:xmlns "http://www.w3.org/1999/xhtml"}
         [:head
           [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
           [:title "Cling: A Clojure Wiki"]
           (get assigns# :head)]
         [:body
           [:div#container
             [:div#session
               [:p "login/logout"]]
             [:div#search
               (form {:to (path-info :search-pages {:query ""})}
                 (html
                   [:p "Search:"]
                   (text-field-tag "query" (get assigns# :query))))]
             [:div#content
               ~@body]]]])))

(defn error-messages-page
  [page]
  (when-let-html [errs (errors page)]
    [:div
      [:h3 "There were problems with your submission:"]
      (for-html [err errs]
        [:p (name (:on err))])]))

(defhtml partial-page-form
  [page]
  [:p "title:"]
  [:p (text-field-tag "page[title]" (:title page))]
  [:p "body:"]
  [:p (text-area-tag  "page[body]"  (:body  page) {:rows 20 :cols 80})]
  [:p (submit-tag (if (new? page) "Create Page" "Update Page"))])

(defn quote-title
  [title]
  (str "&#8220;" (h title) "&#8221;"))

(defhtml page-links
  [page]
   [:ul
     [:li (link-to "page"    (path :show-page          page))]
     [:li (link-to "edit"    (path :edit-page          page))]
     [:li (link-to "history" (path :show-page-versions page))]])

;; Main Views
(defn index-pages
  [pages]
  (layout {}
    [:h1 "Pages"]
    [:ul
      (for-html [page (sort-by :title pages)]
        [:li (link-to (h (:title page)) (path :show-page page))])]
    [:p (link-to "New Page" (path :new-page))]))

(defn index-pages-versions
  [page-versions]
  (layout {:head (auto-discovery-link-tag :atom
                   {:title "Edits for All Pages"
                    :href (url :index-pages-versions-atom)})}
    [:h1 "Edits for All Pages"]
    [:ul
      (for-html [page-version page-versions]
        [:li (link-to (str (h (:title page-version)) " at "
                           (h (str (:updated_at page-version))))
                      (path :show-page-version page-version))])]))

(defxml index-pages-versions-atom
  [page-versions]
  [:decl! {:version "1.1"}]
  [:feed {:xmlns "http://www.w3.org/2005/Atom" "xml:lang" "en-US"}
    [:id (url :index-page-versions-atom)]
    [:title "Edits for All Pages"]
    [:link {:href (url :index-pages-versions-atom) :rel "self" :type "application/rss+xml"}]
    [:updated (time/xmlschema (high (map :updated_at page-versions)))]
    [:generator "Cling"]
    (for [page-version page-versions]
      [:entry
        [:id (url :show-page-version page-version)]
        [:title (h (:title page-version))]
        [:link {:rel "alternate" :type "text/html"
                :href (url :show-page-version page-version)}]
        [:updated
          (time/xmlschema (:updated_at page-version))]
        [:summary {:type "xhtml"}
          [:div {:xmlns "http://www.w3.org/1999/xhtml"}
            (textilize (:body page))]]])])

(defn search-pages
  [query pager]
  (layout {:query query}
    (if (= 0 (total-entries pager))
      (html [:h1 "No Results for " (quote-title query)])
      (html [:h1 "Results for " (quote-title query)]
            [:ul
              (for-html [page (entries pager)]
                [:li (link-to (str (h (:title page))) (path :show-page page))])]))))

(defn new-page
  [page]
  (layout {}
    [:h1 "New Page"]
    (error-messages-page page)
    (form {:to (path-info :create-page)}
      (partial-page-form page))))

(defn show-page
  [page]
  (layout {:head (auto-discovery-link-tag :atom
                   {:title (str "Edits for " (quote-title (:title page)))
                    :href (url :show-page-versions-atom page)})}
    (page-links page)
    [:h2 (h (:title page))]
    [:div (textilize (:body page))]))

(defn show-page-versions
  [page page-versions]
  (layout {:head (auto-discovery-link-tag :atom
                   {:title (str "Edits for " (quote-title (:title page)))
                    :href (url :show-page-versions-atom page)})}
    [:h1 "Edits for " (quote-title (:title page))]
    [:ul
      (for-html [page-version page-versions]
        [:li (link-to (h (str (:updated_at page)))
                      (path :show-page-version page-version))])]))

(defxml show-page-versions-atom
  [page page-versions]
  [:decl! {:version "1.1"}]
  [:feed {:xmlns "http://www.w3.org/2005/Atom" "xml:lang" "en-US"}
    [:id (url :show-page-versions page)]
    [:title "Edits for " (quote-title (:title page))]
    [:link {:href (url :show-page-versions page) :rel "self" :type "application/rss+xml"}]
    [:updated (time/xmlschema (high (map :updated_at page-versions)))]
    [:generator "Cling"]
    (for [page-version page-versions]
      [:entry
        [:id (url :show-page-version page-version)]
        [:title (h (:title page-version))]
        [:link {:rel "alternate" :type "text/html"
                :href (url :show-page-version page-version)}]
        [:updated
          (time/xmlschema (:updated-at page-version))]
        [:summary {:type "xhtml"}
          [:div {:xmlns "http://www.w3.org/1999/xhtml"}
            (textilize (:body page-version))]]])])

(defn show-page-version
  [page page-version]
  (layout {}
    (page-links page)
    [:h2 (h (:title page))]
    [:p "Revision as of " (h (str (:updated_at page-version)))]
    [:div (textilize (:body page))]))

(defn edit-page
  [page]
  (layout {}
    (page-links page)
    [:h2 "Editing " (quote-title (:title page))]
    (error-messages-page page)
    (form {:to (path-info :update-page page)}
      (partial-page-form page))))

(defn not-found
  []
  (layout {}
    [:h3 "We're sorry - we couln't find that."]
    [:p  "Please return to the " (link-to "Home Page" (path :home))]))

(defn internal-error
  []
  (layout {}
    [:h3 "We're sorry - something went wrong."]
    [:p  "We've been notified of the problem and are looking into it."]))