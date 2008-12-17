(ns stash.timestamps
  (:use clj-time.core))

(defn timestamp-create
  "TODOC"
  [instance]
  (let [t (now)]
    [(assoc instance :created-at t :updated-at t) true]))

(defn timestamp-update
  "TODOC"
  [instance]
  [(assoc instance :updated-at (now)) true])