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

(defn not-found []
  (respond-404 (v/not-found)))

(defmacro with-post
  [[binding-sym id-form] & body]
  `(if-let [~binding-sym (stash/get-one m/+post+ ~id-form)]
     (do ~@body)
     (not-found)))

(defn index [req]
  (respond (v/index (stash/find-all m/+post+))))

(defn index-atom [req]
  (respond (v/index-atom (stash/find-all m/+post+))
    {:content-type "application/rss+xml"}))

(defn show [req]
  (with-post [post (params req :id)]
    (respond (v/show post))))

(defn new [req]
  (with-post [post (params req :id)]
    (respond (v/new post))))

(defn edit [req]
  (with-post [post (params req :id)]
    (respond (v/edit post))))

(defn create [req]
  (let [post (stash/create m/+post+ (params req :post))]
    (if (stash/valid? post)
      (redirect (path :post post))
      (respond (v/new post)))))
      ;(flashing :post-create-success (redirect (path :post post)))
      ;(flashing :post-create-error   (response (v/new post))))))

(defn update [req]
  (with-post [post (params req :id)]
    (let [post (stash/save (stash/update-attrs post (params req :post)))]
      (if (stash/valid? post)
        (redirect (path :post post))
        (respond (v/edit post))))))
        ;(flashing :post-update-success (redirect (path :post post)))
        ;(flashing :post-update-failure (response (v/edit post)))))))

(defn destroy [req]
  (with-post [post (params req :id)]
    (stash/destroy post)
    (redirect (path :posts))))
    ;(flashing :post-destroy-success (redirect (path :posts)))))

