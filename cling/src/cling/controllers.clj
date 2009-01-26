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
  (redirect (path :show-page {:slug "home"})))

(defn index-pages [req]
  (respond (views/index-pages
             (stash/find-all models/+page+ {:select [:title :slug]}))))

(defn index-pages-versions [req]
  (respond (views/index-pages-versions
             (stash/find-all models/+page-version+ {:order [:updated_at :desc] :limit 10}))))

(defn index-pages-versions-atom [req]
  (respond-atom (views/index-pages-versions-atom
                  (stash/find-all models/+page-version+ {:order [:updated_at :desc] :limit 10}))))

(defn search-pages [req]
  (let [query (params req :query)
        pager (models/search-pages query)]
    (respond (views/search-pages query pager))))

(defn new-page [req]
  (respond (views/new-page (stash/init models/+page+))))

(defn show-page [req]
  (if-let [page (models/find-page (params req :slug))]
    (respond (views/show-page page))
    (not-found req)))

(defn show-page-versions [req]
  (if-let [[page page-versions] (models/find-page-and-versions (params req :slug))]
    (respond (views/show-page-versions page page-versions))
    (not-found req)))

(defn show-page-versions-atom [req]
  (if-let [[page page-versions] (models/find-page-and-versions (params req :slug))]
    (respond-atom (views/show-page-versions-atom page page-versions))
    (not-found req)))

(defn show-page-version [req]
  (if-let [[page page-version] (models/find-page-and-version (params req :slug) (params req :vid))]
    (respond (views/show-page-version page page-version))
    (not-found req)))

(defn show-page-diff [req]
  (let [p (partial params req)]
    (if-let [[page-version-a page-version-b]
               (models/find-page-version-pair (p :slug) (p :vid) (p :oldvid))]
      (respond (views/show-page-diff page-version-a page-version-b))
      (not-found req))))

(defn edit-page [req]
  (if-let [page (models/find-page (params req :slug))]
    (respond (views/edit-page page))
    (not-found req)))

(defn create-page [req]
  (let [page (stash/create models/+page+ (params req :page))]
    (if (stash/valid? page)
      (redirect (path :show-page page))
      (respond (views/new-page page)))))

(defn update-page [req]
  (if-let [page (models/find-page (params req :slug))]
    (let [page (stash/update page (params req :page))]
      (if (stash/valid? page)
        (redirect (path :show-page page))
        (respond (views/edit-page page))))
    (not-found req)))

(defn destroy-page [req]
  (if-let [page (models/find-page (params req :slug))]
    (do
      (stash/destroy page)
      (redirect (path :index-pages)))
    (not-found req)))
