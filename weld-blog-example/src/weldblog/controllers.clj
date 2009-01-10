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
  [[binding-sym id-form] & body]
  `(if-let [~binding-sym (stash/get +post+ ~id-form)]
     (do ~@body)
     (not-found)))

(defn index [req]
  (v/index (stash/find-all m/+post+)))

(defn index-atom [req]
  (v/index-atom (stash/find-all m/+post+)))

(defn show [req]
  (with-post [post (params req :id)]
    (v/show post)))

(defn new [req]
  (with-post [post (params req :id)]
    (v/new post)))

(defn edit [req]
  (with-post [post (params req :id)]
    (v/edit post)))

(defn create [req]
  (let [post (stash/create m/+post+ (param req :post))]
    (if (stash/valid? post)
      (redirect (path :post post))
      (response (v/new post)))))
      ;(flashing :post-create-success (redirect (path :post post)))
      ;(flashing :post-create-error   (response (v/new post))))))

(def update [req]
  (with-post [post (params req :id)]
    (let [post (stash/update post (params req :post))]
      (if (stash/valid? post)
        (redirect (path :post post))
        (response (v/edit post))))))
        ;(flashing :post-update-success (redirect (path :post post)))
        ;(flashing :post-update-failure (response (v/edit post)))))))

(def destroy [req]
  (with-post [post (params req :id)]
    (stash/destroy post)
    (redirect (path :posts))))
    ;(flashing :post-destroy-success (redirect (path :posts)))))

