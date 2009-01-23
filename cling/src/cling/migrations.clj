(ns cling.migrations
  (:use
    (stash migrations)))

(defmigration create-pages 1
  (create-table :pages
    [[:id :uuid {:pk true :auto true}]
      [:title :string]
      [:body  :string]
      [:created_at :datetime]
      [:updated_at :datetime]])
  (drop-table :pages))

(defmigration add-permalinks 2
  (add-column :pages [:permalink :string {:unique true}])
  (drop-column :pages :permalink))

(def all [create-pages add-permalinks])