(ns ringblog.migrations
  (:use stash.migrations))

(defmigration add-posts 1
  (create-table :posts
    [[:id    :uuid   {:pk true}]
     [:title :string]
     [:body  :string]])
  (drop-table :posts))

(def all
  [add-posts])
