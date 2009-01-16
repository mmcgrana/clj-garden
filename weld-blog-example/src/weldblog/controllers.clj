(ns weldblog.controllers
  (:use
    (weld controller request config)
    (weldblog routing auth))
  (:require
    (weldblog
      [models   :as m]
      [views    :as v]
      [config   :as config])
    [stash.core :as stash]))

(defn not-found [& [env]]
  (respond (v/not-found (session env))))

(defn not-authenticated [& [info]]
  (respond (v/new-session info)))

(defn new-session [env]
  (respond (v/new-session)))

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
  [[post-bind env] & body]
  `(if-let [~post-bind (stash/get-one m/+post+ (params ~env :id))]
     (do ~@body)
     (not-found ~env)))

(defn index [env]
  (with-fading-session []
    (respond (v/index (stash/find-all m/+post+) sess))))

(defn index-atom [env]
  (respond (v/index-atom (stash/find-all m/+post+))
    {:content-type "application/rss+xml"}))

(defn show [env]
  (with-post [post env]
    (with-fading-session [sess env]
      (respond (v/show post sess)))))

(defn new [env]
  (with-auth [sess env]
    (respond (v/new (stash/init m/+post+) sess))))

(defn edit [env]
  (with-auth [sess env]
    (with-post [post env]
      (respond (v/edit post sess)))))

(defn create [env]
  (with-auth [sess env]
    (let [post (stash/create m/+post+ (params env :post))]
      (if (stash/valid? post)
        (flash-session sess :post-created
          (redirect (path :post post)))
        (respond (v/new post sess))))))

(defn update [env]
  (with-auth [sess env]
    (with-post [post env]
      (let [post (stash/save (stash/update-attrs post (params env :post)))]
        (if (stash/valid? post)
          (flash-session sess :post-updated
            (redirect (path :post post)))
          (respond (v/edit post sess)))))))

(defn destroy [env]
  (with-auth [sess env]
    (with-post [post env]
      (stash/destroy post)
      (flash-session sess :post-destroyed
        (redirect (path :posts))))))

(defn with-xtime
  [action]
  (fn [req]
    (let [start (time/now)]
      (let [resp (action req)]
        (assoc-in resp [:headers "X-Runtime"] (- (time/now) start))))))

(defn with-rescues
  [action]
  (fn [req]
    (try
      (action req)
      (catch Exception e
        (respond (v/internal-error) {:status 500})))))

(def index (with-rescues (with-xtime index)))
