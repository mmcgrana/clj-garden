(ns weldblog.controllers
  (:use
    (weld controller request config routing)
    (weldblog auth))
  (:require
    (weldblog [models   :as models]
              [views    :as views]
              [config   :as config])
    [stash.core :as stash]))

(defn not-found [& [env]]
  (respond (views/not-found (session env))))

(defn not-authenticated [& [info]]
  (respond (views/new-session info)))

(defn new-session [env]
  (respond (views/new-session)))

(defn create-session [env]
  (if (= config/admin-password (params env :password))
    (with-session [sess env]
      (flash-session (authenticated sess) :session-created
        (redirect (path :posts))))
    (not-authenticated {:params (params env)})))

(defn destroy-session [env]
  (with-session [sess env]
    (flash-session (unauthenticated sess) :session-destroyed
      (redirect (path :posts)))))

(defmacro with-auth
  [[sess-bind env-form] & body]
  `(with-session [~sess-bind ~env-form]
      (if (authenticated? ~sess-bind)
        (do ~@body)
        (not-authenticated {:message :session-needed}))))

(defmacro with-post
  [[post-bind env-sym] & body]
   `(if-let [~post-bind (stash/get-one models/+post+ (params ~env-sym :id))]
      (do ~@body)
      (not-found ~env-sym)))

(defn index [env]
  (with-fading-session [sess env]
    (respond (views/index (stash/find-all models/+post+) sess))))

(defn index-atom [env]
  (respond (views/index-atom (stash/find-all models/+post+))
    {:content-type "application/rss+xml"}))

(defn show [env]
  (with-post [post env]
    (with-fading-session [sess env]
      (respond (views/show post sess)))))

(defn new [env]
  (with-auth [sess env]
    (respond (views/new (stash/init models/+post+) sess))))

(defn edit [env]
  (with-auth [sess env]
    (with-post [post env]
      (respond (views/edit post sess)))))

(defn create [env]
  (with-auth [sess env]
    (let [post (stash/create models/+post+ (params env :post))]
      (if (stash/valid? post)
        (flash-session sess :post-created
          (redirect (path :post post)))
        (respond (views/new post sess))))))

(defn update [env]
  (with-auth [sess env]
    (with-post [post env]
      (let [post (stash/update post (params env :post))]
        (if (stash/valid? post)
          (flash-session sess :post-updated
            (redirect (path :post post)))
          (respond (views/edit post sess)))))))

(defn destroy [env]
  (with-auth [sess env]
    (with-post [post env]
      (stash/destroy post)
      (flash-session sess :post-destroyed
        (redirect (path :posts))))))
