(ns ringblog.views
  (:use ringblog.routing
        (clj-html core helpers helpers-ext)
        [stash.core :only (errors)]))

(defmacro with-layout
  [& body]
  `(with-layout-throwing {} ~@body))

(defmacro with-layout-throwing
  [thrown & body]
  `(let [inner# (do ~@content)]
     (html
       (doctype :xhtml-transitional)
       [:html {:xmlns "http://www.w3.org/1999/xhtml"}
         [:head
           (include-css "/stylesheets/main.css")
           (thrown :for_head)
           [:meta {:http-equiv "Content-Type" :content "text/html;charset=utf-8"}]
           [:title "Ring Blog Example"]
           [:body inner#]]])))

(defn partial-post
  [post]
  (html
    [:div {:class (str "post_" (:id post))}
      [:h2 (link-to (:title post) (path :post post))]
      [:div.post_body
        (h (:body post))]]))

(defn index
  [posts]
  (with-layout-throwing
    {:for_head
      (atom-autodiscovery-tag "Feed for Ring Blog Example" (path :posts-atom))}
    [:h1 "Posts"]
    [:p (link-to "New Post" (path :new-posts))]
    [:div#posts
      (map-str post-partial posts)]))

(defn index-atom
  [posts]
  (xml
    [:decl! {:version "1.1"}
      [:feed {:xmlns "http://www.w3.org/2005/Atom" "xml:lang" "en-US"}
        [:id (url :posts-atom)]
        [:title "Ring Blog Example"]
        [:updated (time/xmlschema (time/now))]
        [:link {:href (url :posts-atom) :rel "self" :type "application/rss+xml"}]
        [:author
          [:name "Mark McGranaghan"]
          [:email "mmcgrana@gmail.com"]
          [:uri "http://github.com/mmcgrana"]]
        (for [post posts]
        [:entry
          [:id (url :post post)]
          [:title (h (:title post))]
          [:link {:rel "alternate" :type "text/html"
                  :href (url :post post)}]
          [:updated
            (time/xmlschema (time/now))]
          [:summary {:type "xhtml"}
            [:div {:xmlns "http://www.w3.org/1999/xhtml"}
              (h (:body post))]]])]]))

(defn show
  [post]
  (with-layout
    (post-partial post)
    [:p (link-to "All Posts" (path :posts)) " | "
        (link-to "Edit Post" (path :edit-post post))]))

(defn error-messages-post
  [post]
  [:div.error-messages
    [:h3 "There were problems with your submission:"]
    (domap-str [error (errors post)]
      (html [:p (name (:on error))]))])

(defn partial-post-form
  [post]
  (html
    [:p (text-field-tag "post[title]" (:title post))]
    [:p (text-area-tag  "post[body]"  (:body  post) {:rows 20 :cols 80})]
    [:p (submit-tag "Submit Post")]))

(defn new
  [post]
  [:h1 "New Post"]
  (error-messages-post post)
  (form-to (path :create-post)
    (partial-post-form post)))

(defn edit
  [post]
  [:h1 "Editing Post"]
  (error-messages-post post)
  (form-to (path :update-post post)
    (partial-post-form post)))

(defn not-found []
  (with-layout
    [:h3 "We're sorry - we couln't find that."]
    [:p  "Please return to the " (link-to "Home Page" (path :posts))]))

(defn internal-error []
  (with-layout
    [:h3 "We're sorry - something went wrong."]
    [:p  "We've been notified of the problem and are looking into it."]))