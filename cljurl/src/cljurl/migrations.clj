(ns cljurl.migrations
  (:use stash.migrations))

(defmigration bootstrap 1
  (do
    (create-table :shortenings
      [[:id         :uuid   {:pk true}]
       [:slug       :string {:unique true}]
       [:url        :string]
       [:created_at :datetime]])
    (create-table :hits
      [[:id             :uuid {:pk true}]
        [:shortening_id :uuid]
        [:ip            :string]
        [:created_at    :datetime]
        [:updated_at    :datetime]
        [:hit_count     :integer]]))
  (do
    (drop-table :shortenings)
    (drop-table :hits)))

(def all
  [bootstrap])