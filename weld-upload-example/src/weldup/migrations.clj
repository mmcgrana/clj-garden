(ns weldup.migrations
  (:use stash.migrations))

(defmigration bootstrap 1
  (create-table :uploads
    [[:id            :uuid    {:pk true}]
     [:filename      :string]
     [:content_type  :string]
     [:size          :integer]])
  (drop-table :uploads))

(def all [bootstrap])