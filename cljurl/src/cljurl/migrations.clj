(ns cljurl.migrations
  (:use stash.migrations))

(defmigration bootstrap [conn 1]
  (create-table conn :uploads
    [[:id            :uuid    {:pk true}]
     [:filename      :string]
     [:content_type  :string]
     [:size          :integer]])
  (drop-table conn :uploads))

(def all [bootstrap])