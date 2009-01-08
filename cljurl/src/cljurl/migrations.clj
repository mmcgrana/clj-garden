(ns cljurl.migrations
  (:use stash.migrations))

(defmigration bootstrap [conn 1]
  (do
    (create-table conn :shortenings
      [[:id         :uuid   {:pk true}]
       [:slug       :string {:unique true}]
       [:url        :string]
       [:created_at :datetime]])
    (create-table conn :hits
      [[:id             :uuid {:pk true}]
        [:shortening_id :uuid]
        [:ip            :string]
        [:created_at    :datetime]
        [:updated_at    :datetime]
        [:hit_count     :integer]]))
  (do
    (drop-table conn :shortenings)
    (drop-table conn :hits)))

(def all
  [bootstrap])