; data source
; unique-id ?? - interesting
; views (scopes) 
; conditions
; overwriting readers / writers
; attr query methods
; serialized columns
; around save, update, create, destroy, find, validate

; (defgetters +post+)
; (defn title [post] (:title post))
; (let [new-post (title= old-post "new title")] (dosomething new-post))


; src/cljblog/models/post.clj
(ns cljblog.models.post
  (:use stash.def stash.validators stash.timestamps))

(defn find-value-by-sql
  [model sql]
  (let [[result db-time]
          (jdbc/with-connection [conn (data-source model)]
            (jdbc/select-value conn sql))]
    (logging sql time
      result)))

(defn find-value-by-sql
  [model sql]
  (with-logging (logger model) :info sql
    (jdbc/with-connection [conn (data-source model)]
      (jdbc/select-value conn sql))))

(defn find-value-by-sql
  [model sql]
  (with-logging (logger model) :info sql
    (jdbc/with-connection [conn (data-source model)]
      (jdbc/select-value conn sql))))

(in-transaction +post+
  (delete-all +post+)
  (delete-all +comment+))

(defmacro in-transaction [model & body]
  `(jdbc/with-connection [conn (data-source model)]
     (jdbc/in-transaction conn ~@body)))

(defmacro with-logging [logger level sql form]
  `(do
     (if ~logger
       (let [[result# time#] (realtime ~form)]
         (logger ~level (str "(" time# ") " ~sql))
         result#)
       ~form)))

(defn find-one-by-sql
  "Returns an instance of model found by the given sql, or nil if no such
  instances are found. "
  [model sql]
  (if-let [uncast-attrs (jdbc/with-connection [conn (data-source model)]
                          (jdbc/select-map conn sql))]
    (instantiate model uncast-attrs)))

(with-transaction)
(declare gen-slug)
(
[:title      (min-length 10) {:if-not admin?}]]
[:word-count (min-count 10)  {:from word-count}]]

)



(defmacro sfl []
  `(map ^(def x#) [:file :line]))

(defmacro sfl []
  [(var-get clojure.lang.Compiler/SOURCE)
   (var-get clojure.lang.Compiler/LINE)])

1:39 user=> (source-file-line)
["repl-1" 39]


  (def (gensym)))
(ns 'app.models.post
  (:use stash.timestamps stash.validators app.config.db))

(declare gen-slug word-count)

(defmodel +post+
  {:data-source +data-source+
   :table-name  :posts
   :column
     [[:title      :string   {:width 20}]
      [:body       :string   {:width 5000}
      [:slug       :string   {:width 50}]
      [:posted-at  :datetime]
      [:created-at :datetime]
      [:updated-at :datetime]
      [:num-views  :integer  {:default 0}]]]
   :validations
     [[:title      presence]
      [:body       presence]
      [:word-count (min-count 10) {:virtual true}]]
   :callbacks
     {:before-validate
        [gen-slug]
      :before-create
        [timestamp-create]
      :before-update
        [timestamp-update]}
    :accesible-attrs
      [:title :body]})
(defaccessors +post+ {:except [:title?]})

(def word-count [post]
  (str-lib/word-count (body post)))

(redef-by +post+ with-versioning)
; with-column
; with-validation
; with-callback

or
(defmodel +post+
  {:data-source +data-source+
   :table-name :posts
   :columns
     []
   :extensions
     [is-versioned is-paranoid (is-permalinked {:on :title})]})
; here extension fns take a model hash and return a new, extended one

(defn slugify-title
  [title]
  (re-gsub #"[^a-z0-9-]" "-" (.toLowerCase title)))

(defn gen-slub
  [instance]
  (assoc instance :slug (slugify-title (:title post))))

(defn publish
  [instance]
  (save (assoc instance :published-at (time/now))))

; src/cljblog/controllers/posts.clj
(ns cljblog.controllers.posts
  (:use cljblog.models.post))

(defn show
  [request]
  (let-or-not-found [post (one-by +post+ :slug (params request :slug))]
    (view/show post)))

; src/cljblog/views/posts.clj
(ns cljblog.views.posts
  (:use cljblog.views.helpers))

(defn show
  [post]
  (html-in-layout
    [:h1 (h (:title post))]
    [:h2 "Posted at:" (sprintf "%M %d, %Y" (:posted-at post))]
    [:div.body
      (safe-markdown (:body post))]))


;;;;;
(defn create
  [request]
  (if-let [user (auth/auth-by +user+ (params request :username)
                                     (params request :password))]
    (session-assoc :user (:id user)
      (if-let [return-to (session-get request :return-to)]
        (session-dissoc :return-to
          (redirect return-to))
        (redirect (path :root))
    (v/new {:error :bad-credentials})))

(defn destroy
  [request]
  (cookies-dissoc request :auth-token))


  (defn presence
    "Returns a presence validator for attr-name."
    [attr-name]
    (let [error (struct +error+ attr-name :presence)]
      (fn [instance]
        (if (nil? (attr-name instance))
          error))))

  (defn min-length
    "Returns a minimum length validator for attr-name requiring at least a
    specified length."
    [attr-name length]
    (let [error (struct +error+ attr-name :min-length length)]
      (fn [instance]
        (let [val (attr-name instance)]
          (if (or (nil? val) (< (.length val) length))
            error)))))


;;;;;;;

{:current-user user}

(ns cljurl.permissions)

(if (permitted? user :foo)
  "Special secret stuff")
(def permitted?
  [user thing])


(defn update
  [request]
  (if-let [post (finders/one-by-id +post+ (params request :id))]
    (let [post (crud/form-update post (params request :post))]
      (if (errors? post)
        (success (v/edit post))
        (redirect (path :post post))))
    (not-found (v/not-found))))

(defn update
  [request]
  (let-or-not-found [post (one-by-id +post+ (params request :id))]
    (if-let-valid [post (form-update post (params request :post))]
      (redirect (path :post post))
      (success  (v/edit post)))))
