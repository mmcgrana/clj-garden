(ns weldblog.controllers
  (:use
    (weld controller request routing)
    (weldblog auth utils))
  (:require
    (weldblog [models   :as models]
              [views    :as views])
    (stash [core :as stash]
           [pagination :as pagination])))

;; Helpers
(defn not-authenticated [sess]
  (respond (views/new-session sess)))

(defmacro with-auth
  [[sess-bind req-form] & body]
  `(with-session [~sess-bind ~req-form]
      (if (authenticated? ~sess-bind)
        (do ~@body)
        (not-authenticated (assoc ~sess-bind :flash :session-needed)))))

(defmacro with-post
  [[post-bind req-sym] & body]
   `(if-let [~post-bind (stash/get-one models/+post+ (params ~req-sym :id))]
      (do ~@body)
      (not-found ~req-sym)))

;; Main Actions
(defn not-found [req]
  (respond (views/not-found (session req)) {:status 404}))

(defn internal-error [req]
  (respond (views/internal-error (session req)) {:status 500}))

(defn home [req]
  (redirect (path :index-posts)))

(defn new-session [req]
  (with-session [sess req]
    (respond (views/new-session sess))))

(defn create-session [req]
  (with-session [sess req]
    (if (authenticate? (params req :password))
      (flash-session (authenticated sess) :session-created
        (redirect (path :index-posts)))
      (not-authenticated sess))))

(defn destroy-session [req]
  (with-session [sess req]
    (flash-session (unauthenticated sess) :session-destroyed
      (redirect (path :index-posts)))))

(defn index-posts [req]
  (with-fading-session [sess req]
    (let [pager (pagination/paginate models/+post+
                  {:page (to-int (params req :page))
                   :per-page 2 :order [:created_at :asc]})]
      (respond (views/index sess pager)))))

(defn index-posts-atom [req]
  (let [posts (stash/find-all models/+post+
                {:limit 4 :order [:created_at :asc]})]
    (respond (views/index-atom posts) {:content-type "application/rss+xml"})))

(defn new-post [req]
  (with-auth [sess req]
    (respond (views/new sess (stash/init models/+post+)))))

(defn show-post [req]
  (with-post [post req]
    (with-fading-session [sess req]
      (respond (views/show sess post)))))

(defn edit-post [req]
  (with-auth [sess req]
    (with-post [post req]
      (respond (views/edit sess post)))))

(defn create-post [req]
  (with-auth [sess req]
    (let [post (stash/create models/+post+ (params req :post))]
      (if (stash/valid? post)
        (flash-session sess :post-created
          (redirect (path :show-post post)))
        (respond (views/new sess post))))))

(defn update-post [req]
  (with-auth [sess req]
    (with-post [post req]
      (let [post (stash/update post (params req :post))]
        (if (stash/valid? post)
          (flash-session sess :post-updated
            (redirect (path :show-post post)))
          (respond (views/edit sess post)))))))

(defn destroy-post [req]
  (with-auth [sess req]
    (with-post [post req]
      (stash/destroy post)
      (flash-session sess :post-destroyed
        (redirect (path :index-posts))))))
