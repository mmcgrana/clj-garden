(ns weldblog.controllers
  (:use
    (weld controller request)
    weldblog.routing)
  (:require
    (weldblog
      [models   :as m]
      [views    :as v]
      [config   :as config])
    [stash.core :as stash]))

(defmacro with-post
  [[post-bind id-form] & body]
  `(if-let [~post-bind (stash/get-one m/+post+ ~id-form)]
     (do ~@body)
     (not-found)))

(defn index [env]
  (with-fading-session [sess env]
    (respond (v/index (stash/find-all m/+post+) sess))))

(defn index-atom [env]
  (respond (v/index-atom (stash/find-all m/+post+))
    {:content-type "application/rss+xml"}))

(defn show [env]
  (with-post [post (params env :id)]
    (with-fading-session [sess env]
      (respond (v/show post sess)))))

(defn new [env]
  (respond (v/new (stash/init m/+post+))))

(defn edit [env]
  (with-post [post (params env :id)]
    (respond (v/edit post))))

(defn create [env]
  (let [post (stash/create m/+post+ (params env :post))]
    (if (stash/valid? post)
      (flash-env env {:success :post-create}
        (redirect (path :post post)))
      (respond (v/new post)))))

(defn update [env]
  (with-post [post (params env :id)]
    (let [post (stash/save (stash/update-attrs post (params env :post)))]
      (if (stash/valid? post)
        (flash-env env {:success :post-update}
          (redirect (path :post post)))
        (respond (v/edit post))))))

(defn destroy [env]
  (with-post [post (params env :id)]
    (stash/destroy post)
    (flash-env env {:success :post-destroy}
      (redirect (path :posts)))))

