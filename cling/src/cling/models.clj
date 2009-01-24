(ns cling.models
  (:use
    (stash core validators timestamps pagination)
    (clojure.contrib str-utils))
  (:require
    (cling [config :as config])))

(def model-base
  {:data-source config/data-source
   :logger      config/logger})

(declare add-permalink set-version save-version)

(defmodel +page+
  (merge model-base
  {:table-name  :pages
   :columns
     [[:id         :uuid {:pk true :auto true}]
      [:vid        :uuid ]
      [:title      :string]
      [:permalink  :string]
      [:body       :string]
      [:created_at :datetime]
      [:updated_at :datetime]]
   :accessible-attrs
     [:title :body]
   :callbacks
      {:before-create [timestamp-create timestamp-update #'add-permalink #'set-version]
       :before-update [timestamp-update #'set-version]
       :after-save    [#'save-version]}}))

(defmodel +page-version+
  (merge model-base
  {:table-name :page_versions
   :columns
     [[:id          :uuid]
       [:vid        :uuid {:pk true}]
       [:title      :string]
       [:permalink  :string]
       [:body       :string]
       [:created_at :datetime]
       [:updated_at :datetime]]}))

(defn format-permalink
  [title]
  (let [p (.toLowerCase title)
        p (re-gsub #"[^a-z0-9]" "-" p)
        p (re-gsub #"-{2,}" "-" p)
        p (re-gsub #"(^-)|(-$)" "" p)]
    p))

(defn add-permalink
  [page]
  [(assoc page :permalink (format-permalink (:title page))) true])

(defn set-version
  [page]
  [(assoc page :vid (auto-uuid)) true])

(defn save-version
  [page]
  [(create* +page-version+ page) true])

(defn find-page
  [permalink]
  (find-one +page+ {:where [:permalink := permalink]}))

(defn find-page-and-versions
  [permalink]
  (if-let [page (find-page permalink)]
    (let [page-versions (find-all +page-version+
                          {:where [:id := (:id page)] :order [:created_at :asc]})]
      [page page-versions])))

(defn find-page-and-version
  [permalink vid]
  (if-let [page (find-page permalink)]
    (if-let [page-version (find-one +page-version+ {:where [:and [:id := (:id page)] [:vid := vid]]})]
      [page page-version])))

(defn search-pages
  [query]
  (let [simple-query (re-gsub #"(?i)[^a-z\s]" "" query)]
    (paginate +page+ {:where (str "body LIKE '%" simple-query "%'")
                      :limit 10})))
