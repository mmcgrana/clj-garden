(ns weldblog.migrations
  (:use stash.migrations))

(defmigration add-posts 1
  (create-table :posts
    [[:id    :uuid   {:pk true}]
     [:title :string]
     [:body  :string]])
  (drop-table :posts))

(defmigration add-timestamps 2
  (do
    (add-column :posts [:created_at :datetime])
    (add-column :posts [:updated_at :datetime]))
  (do
    (drop-column :posts :created_at)
    (drop-column :posts :updated_at)))

(def all
  [add-posts add-timestamps])
