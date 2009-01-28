(ns cling.models
  (:use
    (stash core validators timestamps pagination)
    (clojure.contrib str-utils))
  (:require
    (cling [config :as config])))

(def model-base
  {:data-source config/data-source
   :logger      config/logger})

(declare add-slug set-version save-version)

(defmodel +page+
  (merge model-base
  {:table-name  :pages
   :columns
     [[:id         :uuid {:pk true :auto true}]
      [:vid        :uuid ]
      [:title      :string]
      [:slug       :string]
      [:body       :string]
      [:created_at :datetime]
      [:updated_at :datetime]]
   :accessible-attrs
     [:title :body]
   :callbacks
      {:before-create [timestamp-create timestamp-update #'add-slug #'set-version]
       :before-update [timestamp-update #'set-version]
       :after-save    [#'save-version]}}))

(defmodel +page-version+
  (merge model-base
  {:table-name :page_versions
   :columns
     [[:id          :uuid]
       [:vid        :uuid {:pk true}]
       [:title      :string]
       [:slug       :string]
       [:body       :string]
       [:created_at :datetime]
       [:updated_at :datetime]]}))

(defn format-slug
  [title]
  (let [p (.toLowerCase title)
        p (re-gsub #"[^a-z0-9]" "-" p)
        p (re-gsub #"-{2,}" "-" p)
        p (re-gsub #"(^-)|(-$)" "" p)]
    p))

(defn add-slug
  [page]
  [(assoc page :slug (format-slug (:title page))) true])

(defn set-version
  [page]
  [(assoc page :vid (auto-uuid)) true])

(defn save-version
  [page]
  [(create* +page-version+ page) true])

(defn find-page
  [slug]
  (find-one +page+ {:where {:slug slug}}))

(defn find-page-and-versions
  [slug]
  (if-let [page (find-page slug)]
    (let [page-versions (find-all +page-version+
                          {:where {:id (:id page)} :order [:updated_at :desc]})]
      [page page-versions])))

(defn find-page-and-version
  [slug vid]
  (if-let [page (find-page slug)]
    (if-let [page-version (find-one +page-version+ {:where {:id (:id page) :vid vid}})]
      [page page-version])))

(defn find-page-version-pair
  [slug vid oldvid]
  (if-let [page-version-b (find-one +page-version+ {:where {:slug slug :vid vid}})]
    (if oldvid
      (if-let [page-version-a (find-one +page-version+ {:where {:slug slug :vid oldvid}})]
        [page-version-a page-version-b])
      (let [page-version-a (find-one +page-version+
                             {:where [:and [:slug := slug] [:updated_at :< (:updated_at page-version-b)]]
                              :order [:updated_at :desc]})]
        [page-version-a page-version-b]))))

(defn search-pages
  [query]
  (let [simple-query (re-gsub #"(?i)[^a-z\s]" "" query)]
    (paginate +page+ {:where (str "body LIKE '%" simple-query "%'")
                      :limit 10})))
