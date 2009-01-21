(ns weldblog.controllers
  (:use
    (weld controller request config routing)
    (weldblog auth))
  (:require
    (weldblog [models   :as models]
              [views    :as views]
              [config   :as config])
    [stash.core :as stash]))

;; Helpers
(defn not-authenticated [sess]
  (respond (views/new-session sess)))

(defmacro with-auth
  [[sess-bind env-form] & body]
  `(with-session [~sess-bind ~env-form]
      (if (authenticated? ~sess-bind)
        (do ~@body)
        (not-authenticated (assoc ~sess-bind :flash :session-needed)))))

(defmacro with-post
  [[post-bind env-sym] & body]
   `(if-let [~post-bind (stash/get-one models/+post+ (params ~env-sym :id))]
      (do ~@body)
      (not-found ~env-sym)))

;; Main Actions
(defn not-found [env]
  (respond (views/not-found (session env))))

(defn new-session [env]
  (with-session [sess env]
    (respond (views/new-session sess))))

(defn create-session [env]
  (with-session [sess env]
    (if (= config/admin-password (params env :password))
      (flash-session (authenticated sess) :session-created
        (redirect (path :posts)))
      (not-authenticated sess))))

(defn destroy-session [env]
  (with-session [sess env]
    (flash-session (unauthenticated sess) :session-destroyed
      (redirect (path :posts)))))

(defn index [env]
  (with-fading-session [sess env]
    (respond (views/index sess (stash/find-all models/+post+)))))

(defn index-atom [env]
  (respond (views/index-atom (stash/find-all models/+post+))
    {:content-type "application/rss+xml"}))

(defn new [env]
  (with-auth [sess env]
    (respond (views/new sess (stash/init models/+post+)))))

(defn show [env]
  (with-post [post env]
    (with-fading-session [sess env]
      (respond (views/show sess post)))))

(defn edit [env]
  (with-auth [sess env]
    (with-post [post env]
      (respond (views/edit sess post)))))

(defn create [env]
  (with-auth [sess env]
    (let [post (stash/create models/+post+ (params env :post))]
      (if (stash/valid? post)
        (flash-session sess :post-created
          (redirect (path :post post)))
        (respond (views/new sess post))))))

(defn update [env]
  (with-auth [sess env]
    (with-post [post env]
      (let [post (stash/update post (params env :post))]
        (if (stash/valid? post)
          (flash-session sess :post-updated
            (redirect (path :post post)))
          (respond (views/edit sess post)))))))

(defn destroy [env]
  (with-auth [sess env]
    (with-post [post env]
      (stash/destroy post)
      (flash-session sess :post-destroyed
        (redirect (path :posts))))))
