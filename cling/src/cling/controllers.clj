(ns cling.controllers
  (:use
    (weld controller request routing))
  (:require
    (cling [models   :as models]
           [views    :as views])
    (stash [core :as stash])))

;; Helpers
(defmacro with-page
  [[page-bind req-sym & [id-form]] & body]
   `(if-let [~page-bind (stash/get-one models/+page+ (or ~id-form (params ~req-sym :id)))]
      (do ~@body)
      (not-found ~req-sym)))

;; Main Actions
(defn not-found [req]
  (respond (views/not-found) {:status 404}))

(defn internal-error [req]
  (respond (views/internal-error) {:status 500}))

(defn home [req]
  (if-let [page (models/find-page-by-permalink "home")]
    (respond (views/show-page page))
    (not-found req)))

(defn index-pages [req]
  (respond (views/index-pages
             (stash/find-all models/+page+ {:select [:title :permalink]}))))

(defn new-page [req]
  (respond (views/new-page (stash/init models/+page+))))

(defn show-page [req]
  (if-let [page (models/find-page-by-permalink (params req :permalink))]
    (respond (views/show-page page))
    (not-found req)))

(defn edit-page [req]
  (if-let [page (stash/get-one  models/+page+ (params req :id))]
    (respond (views/edit-page page))
    (not-found req)))

(defn create-page [req]
  (let [page (stash/create models/+page+ (params req :page))]
    (if (stash/valid? page)
      (redirect (path :show-page page))
      (respond (views/new-page page)))))

(defn update-page [req]
  (if-let [page (stash/get-one models/+page+ (params req :id))]
    (let [page (stash/update page (params req :page))]
      (if (stash/valid? page)
        (redirect (path :show-page page))
        (respond (views/edit-page page))))
    (not-found req)))

(defn destroy-page [req]
  (if-let [page (stash/get-one models/+page+ (params req :id))]
    (do
      (stash/destroy page)
      (redirect (path :index-pages)))
    (not-found req)))
