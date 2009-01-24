(ns cling.migrations
  (:use
    (stash migrations)))

(defmigration create-pages 1
  (create-table :pages
    [[:id          :uuid     {:pk true :auto true}]
     [:title      :string]
     [:body       :string]
     [:created_at :datetime]
     [:updated_at :datetime]])
  (drop-table :pages))

(defmigration add-permalinks 2
  (add-column :pages [:permalink :string {:unique true}])
  (drop-column :pages :permalink))

(defmigration add-page-versions 3
  (do
    (add-column :pages [:vid :uuid])
    (create-table :page_versions
      [[:id         :uuid]
       [:vid        :uuid     {:pk true}]
       [:title      :string]
       [:permalink  :string]
       [:body       :string]
       [:created_at :datetime]
       [:updated_at :datetime]]))
  (do
    (drop-column :pages :vid)
    (drop-table :page_versions)))

(def all [create-pages add-permalinks add-page-versions])
