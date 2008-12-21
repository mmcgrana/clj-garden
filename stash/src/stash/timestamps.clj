(ns stash.timestamps
  (:use clj-time.core))

(defn timestamp-create
  "Callback to set the instance's created_at and updated_at values to the time
  this function is called."
  [instance]
  (let [t (now)]
    [(assoc instance :created_at t :updated_at t) true]))

(defn timestamp-update
  "Callback to set the instances updated_at value to the time this function
  is called."
  [instance]
  [(assoc instance :updated_at (now)) true])