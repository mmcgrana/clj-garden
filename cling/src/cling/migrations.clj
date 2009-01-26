(ns cling.migrations
  (:use
    (stash migrations)))

(defmigration create-pages 1
  (create-table :pages
    [[:id         :uuid     {:pk true}]
     [:title      :string]
     [:body       :string]
     [:created_at :datetime]
     [:updated_at :datetime]])
  (drop-table :pages))

(defmigration add-permalinks 2
  (add-column :pages [:slug :string {:unique true}])
  (drop-column :pages :slug))

(defmigration add-page-versions 3
  (do
    (add-column :pages [:vid :uuid])
    (create-table :page_versions
      [[:id         :uuid]
       [:vid        :uuid     {:pk true}]
       [:title      :string]
       [:slug  :string]
       [:body       :string]
       [:created_at :datetime]
       [:updated_at :datetime]]))
  (do
    (drop-column :pages :vid)
    (drop-table :page_versions)))

(defmigration permalink->slug 4
  (do
    (rename-column :pages :slug :slug)
    (rename-column :page_versions :slug :slug))
  (do
    (rename-column :pages :slug :slug)
    (rename-column :page_versions :slug :slug)))

(def all [create-pages add-permalinks add-page-versions permalink->slug])
