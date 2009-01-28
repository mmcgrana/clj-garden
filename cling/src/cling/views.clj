(ns cling.views
  (:use
    (clojure.contrib str-utils fcase)
    (weld routing)
    (clj-html core utils helpers helpers-ext)
    (stash [core :only (errors new?)]
           [pagination :except (paginate)])
    (cling markup diff utils))
  (:require
    (clj-time [core :as time])
    (stash    [core :as stash])))

;; Helpers

(defn path*
  [route-name params]
  (let [[method path unused-params] (path-info route-name params)]
    (str path "?"
      (str-join "&" (for [[key val] unused-params] (str (name key) "=" val))))))

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
     [:li (link-to "new page" (path :new-page))]
     [:li (link-to "page"     (path :show-page          page))]
     [:li (link-to "edit"     (path :edit-page          page))]
     [:li (link-to "history"  (path :show-page-versions page))]])

(defn format-datetime
  [datetime]
  (h (.toString datetime "Y/MM/dd kk:mm")))

(defhtml page-diff
  [page-version-a page-version-b]
  [:table.diff
    [:col.diff-marker] [:col.diff-content] [:col.diff-marker] [:col.diff-content]
    [:tbody
      [:tr
        [:td {:colspan 2}
          (link-to (format-datetime (:updated_at page-version-a))
                   (path :show-page-version page-version-a))]
        [:td {:colspan 2}
          (link-to (format-datetime (:updated_at page-version-b))
                   (path :show-page-version page-version-b))]]
      (let [[_ _ cdiffs] (column-diff-text (:body page-version-a)
                                           (:body page-version-b))]
        (for-html [[[lineno-a lineno-b] ldiffs] cdiffs]
          [:tr
            [:td {:colspan 2} "Line " (inc lineno-a)]
            [:td {:colspan 2} "Line " (inc lineno-b)]]
          (for-html [[type line-a line-b] ldiffs]
            [:tr
              (case type
                :change
                  (html [:td "-"] [:td (h line-a)] [:td "+"] [:td (h line-b)])
                :addition
                  (html [:td]     [:td]            [:td "+"] [:td (h line-b)])
                :deletion
                  (html [:td "-"] [:td (h line-b)] [:td]     [:td]))])))]])
;; Main Views
(defn index-pages
  [pages]
  (layout {}
    [:ul
      [:li (link-to "New Page" (path :new-page))]
      [:li (link-to "Page Edits" (path :index-pages-versions))]]
    [:h1 "Pages"]
    [:ul
      (for-html [page (sort-by :title pages)]
        [:li (link-to (h (:title page)) (path :show-page page))])]))

(defn index-pages-versions
  [page-versions]
  (layout {:head (auto-discovery-link-tag :atom
                   {:title "Edits for All Pages"
                    :href (url :index-pages-versions-atom)})}
    [:h1 "Edits for All Pages"]
    [:ul
      (for-html [page-version page-versions]
        [:li (link-to (str (format-datetime (:updated_at page-version)) " "
                           (quote-title (:title page-version)))
                      (path* :show-page-diff (select-keys page-version '(:slug :vid))))])]))

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
    [:h1 (h (:title page))]
    [:div (textilize (:body page))]))

(defn show-page-versions
  [page page-versions]
  (layout {:head (auto-discovery-link-tag :atom
                   {:title (str "Edits for " (quote-title (:title page)))
                    :href (url :show-page-versions-atom page)})}
    [:h1 "Edits for " (quote-title (:title page))]
    (form {:to (path-info :show-page-diff page)}
      (html
        (submit-tag "Compare selected versions")
        [:ul
          (for-html [page-version page-versions]
            "<input type='radio' name='oldvid' value='" (:vid page-version)"' />"
            "<input type='radio' name='vid'    value='" (:vid page-version)"' />"
            [:li (link-to (format-datetime (:updated_at page-version))
                          (path :show-page-version page-version))])]))))

; This is the good atom feed
(defxml show-page-versions-atom
  [page page-versions]
  [:decl! {:version "1.0" :encoding "utf-8"}]
  [:feed {:xmlns "http://www.w3.org/2005/Atom" "xml:lang" "en"}
    [:id (url :show-page-versions page)]
    [:title {:type "html"} "Edits for " (quote-title (:title page))]
    [:link {:href (url :show-page-versions page) :rel "self" :type "application/rss+xml"}]
    [:updated (time/xmlschema (high (map :updated_at page-versions)))]
    [:author [:name "foobar"]]
    [:generator "Cling"]
    (for [[page-version page-version-prev] (partition 2 1 page-versions)]
      [:entry
        [:id (url :show-page-version page-version)]
        [:title (h (:title page-version))]
        [:link {:rel "alternate" :type "text/html"
                :href (url :show-page-version page-version)}]
        [:updated
          (time/xmlschema (:updated-at page-version))]
        [:content {:type "html" }
          [:cdata!
            (html
              [:div {:xmlns "http://www.w3.org/1999/xhtml"}
                (page-diff page-version-prev page-version)])]]])])

(defn show-page-version
  [page page-version]
  (layout {}
    (page-links page)
    [:h2 (h (:title page))]
    [:p "Revision as of " (h (str (:updated_at page-version)))]
    [:div (textilize (:body page))]))

(defn show-page-diff
  [page-version-a page-version-b]
  (layout {}
    [:h2 "Diff of " (quote-title (:title page-version-a))]
    (page-diff page-version-a page-version-b)))

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