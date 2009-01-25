(ns weldblog.views
  (:use
    (weld routing)
    (weldblog utils auth)
    (clj-html core utils helpers helpers-ext)
    [stash.core :only (errors)]
    [stash.pagination :except (paginate)])
  (:require
    [clj-time.core :as time]
    [stash.core :as stash]))

;; Helpers
(defmacro layout
  [assigns-form & body]
  `(let [assigns# ~assigns-form]
     (html
       (doctype :xhtml-transitional)
       [:html {:xmlns "http://www.w3.org/1999/xhtml"}
         [:head
           (include-css "/stylesheets/main.css")
           (get assigns# :head)
           [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
           [:title "Weld Blog Example"]
           [:body
             [:div#container
               [:div#header
                 [:p.session
                   (if (authenticated? (get assigns# :sess))
                     (delete-button "log out" (path :destroy-session))
                     (link-to "log in"  (path :new-session)))]]
               [:div#content
                 ~@body]]]]])))

(def message-info
  {:session-needed    [:notice  "Please log in"]
   :session-created   [:success "You are now logged in"]
   :session-destroyed [:notice  "You are now logged out"]
   :post-created      [:success "Post created"]
   :post-updated      [:success "Post updated"]
   :post-destroyed    [:success "Post destroyed"]})

(defhtml message-flash
  [sess]
  (when-let-html [[type text] (message-info (get sess :flash))]
    [:h3.message {:class (name type)} text]))

(defhtml partial-post
  [post]
  [:div {:class (str "post_" (:id post))}
    [:h2 (link-to (h (:title post)) (path :show-post post))]
    [:div.post_body
      (h (:body post))]])

(defhtml error-messages-post
  [post]
  (when-let-html [errs (errors post)]
    [:div.error-messages
      [:h3 "There were problems with your submission:"]
      (for-html [err errs]
        [:p (name (:on err))])]))

(defhtml partial-post-form
  [post]
  [:p "title:"]
  [:p (text-field-tag "post[title]" (:title post))]
  [:p "body:"]
  [:p (text-area-tag  "post[body]"  (:body  post) {:rows 20 :cols 80})]
  [:p (submit-tag "Submit Post")])

(defhtml pagination-links
  [pager]
  (let-html [p  (page pager)
             tp (total-pages pager)]
    [:p.pagination
      [:span.pagination-link
        (cond (= p 1) "newer"
              (= p 2) (link-to "newer" (path :index-posts))
              :else   (link-to "newer" (path :index-posts-paginated {:page (dec p)})))]
      " "
      [:span.pagination-link
        (cond (= p tp) "older"
              :else    (link-to "older" (path :index-posts-paginated {:page (inc p)})))]]))

;; Main Views
(defhtml new-session [sess]
  (layout {:sess sess}
    (message-flash sess)
    [:p "password:"]
    (form {:to (path-info :create-session)}
      (html [:p (password-field-tag "password")]))))

(defhtml index
  [sess pager]
  (layout
    {:sess sess
     :head (auto-discovery-link-tag :atom
             {:title "Feed for Ring Blog Example" :href (path :index-posts-atom)})}
    (message-flash sess)
    [:h1 "Posts"]
    (when-html (authenticated? sess)
      [:p (link-to "New Post" (path :new-post))])
    [:div#posts
      (map-str partial-post (entries pager))]
    (pagination-links pager)))

(defxml index-atom
  [posts]
  [:decl! {:version "1.1"}]
  [:feed {:xmlns "http://www.w3.org/2005/Atom" "xml:lang" "en-US"}
    [:id (url :index-posts-atom)]
    [:title "Ring Blog Example"]
    [:updated (time/xmlschema (time/now))]
    [:link {:href (url :index-posts-atom) :rel "self" :type "application/rss+xml"}]
    [:author
      [:name "Mark McGranaghan"]
      [:email "mmcgrana@gmail.com"]
      [:uri "http://github.com/mmcgrana"]]
    (for [post posts]
      [:entry
        [:id (url :show-post post)]
        [:title (h (:title post))]
        [:link {:rel "alternate" :type "text/html"
                :href (url :show-post post)}]
        [:updated
          (time/xmlschema (time/now))]
        [:summary {:type "xhtml"}
          [:div {:xmlns "http://www.w3.org/1999/xhtml"}
            (h (:body post))]]])])

(defhtml show
  [sess post]
  (layout {:sess sess}
    (message-flash sess)
    (partial-post post)
    [:p (link-to "All Posts" (path :index-posts))
      (when-html (authenticated? sess)
        " | " (link-to "Edit Post" (path :edit-post post)))]))

(defhtml new
  [sess post]
  (layout {:sess sess}
    [:h1 "New Post"]
    (error-messages-post post)
    (form {:to (path-info :create-post)}
      (partial-post-form post))))

(defhtml edit
  [sess post]
  (layout {:sess sess}
    [:h1 "Edit Post"]
    (error-messages-post post)
    (form {:to (path-info :update-post post)}
      (partial-post-form post))))

(defhtml not-found
  [sess]
  (layout {:sess sess}
    [:h3 "We're sorry - we couln't find that."]
    [:p  "Please return to the " (link-to "Home Page" (path :index-posts))]))

(defhtml internal-error
  [sess]
  (layout {:sess sess}
    [:h3 "We're sorry - something went wrong."]
    [:p  "We've been notified of the problem and are looking into it."]))