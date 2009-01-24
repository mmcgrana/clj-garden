(ns cling.controllers
  (:use
    (weld controller request routing))
  (:require
    (cling [models   :as models]
           [views    :as views])
    (stash [core :as stash])))

;; Helpers

(defn respond-atom [content]
  (respond content {:content-type "application/rss+xml"}))

;; Main Actions
(defn not-found [req]
  (respond (views/not-found) {:status 404}))

(defn internal-error [req]
  (respond (views/internal-error) {:status 500}))

(defn home [req]
  (redirect (path :show-page {:permalink "home"})))

(defn index-pages [req]
  (respond (views/index-pages
             (stash/find-all models/+page+ {:select [:title :permalink]}))))

(defn index-pages-versions [req]
  (respond (views/index-pages-versions
             (stash/find-all models/+page-version+ {:order [:updated_at :asc] :limit 10}))))

(defn index-pages-versions-atom [req]
  (respond-atom (views/index-pages-versions-atom
                  (stash/find-all models/+page-version+ {:order [:updated_at :asc] :limit 10}))))

(defn search-pages [req]
  (respond (views/search-pages (models/search-pages (params req :query)))))

(defn new-page [req]
  (respond (views/new-page (stash/init models/+page+))))

(defn show-page [req]
  (if-let [page (models/find-page (params req :permalink))]
    (respond (views/show-page page))
    (not-found req)))

(defn show-page-versions [req]
  (if-let [[page page-versions] (models/find-page-and-versions (params req :permalink))]
    (respond (views/show-page-versions page page-versions))
    (not-found req)))

(defn show-page-versions-atom [req]
  (if-let [[page page-versions] (models/find-page-and-versions (params req :permalink))]
    (respond-atom (views/show-page-versions-atom page page-versions))
    (not-found req)))

(defn show-page-version [req]
  (if-let [[page page-version] (models/find-page-and-version (params req :permalink) (params req :vid))]
    (respond (views/show-page-version page page-version))
    (not-found req)))

(defn edit-page [req]
  (if-let [page (models/find-page (params req :permalink))]
    (respond (views/edit-page page))
    (not-found req)))

(defn create-page [req]
  (let [page (stash/create models/+page+ (params req :page))]
    (if (stash/valid? page)
      (redirect (path :show-page page))
      (respond (views/new-page page)))))

(defn update-page [req]
  (if-let [page (models/find-page (params req :permalink))]
    (let [page (stash/update page (params req :page))]
      (if (stash/valid? page)
        (redirect (path :show-page page))
        (respond (views/edit-page page))))
    (not-found req)))

(defn destroy-page [req]
  (if-let [page (models/find-page (params req :permalink))]
    (do
      (stash/destroy page)
      (redirect (path :index-pages)))
    (not-found req)))
