(ns cljurl.app.models
  (:use    stash.core
           stash.timestamps stash.validators
           cljurl.utils)
  (:require [cljurl.config :as config]))


(def +slug-chars+  [\a \b \c \d \e \f \g \h \i \j \k \1 \2 \3 \4 \5])
(def +slug-length+ 5)

(defn generate-slug
  "Callbacks returning an instance of the shortening with its new slug."
  [shortening]
  (let [slug (str-cat (take +slug-length+ (map choice (repeat +slug-chars+))))]
    [(assoc shortening :slug slug) true]))

(defn zero-hit-count
  [shortening]
  [(assoc shortening :hit_count 0) true])

(defmodel +shortening+
  {:data-source config/+data-source+
   :table-name  :shortenings
   :columns
     [[:slug       :string]
      [:url        :string]
      [:hit_count  :integer]
      [:created_at :datetime]]
   :accessible-attrs
     [:url]
   :validations
     [[:url valid-url]]
   :callbacks
     {:before-create [timestamp-create generate-slug zero-hit-count]}})

(defn find-recent-shortenings
  "Returns the n most recently created shortenings."
  [n]
  (find-all +shortening+ {:limit n :order [:created_at :desc]}))

(defn inc-attr [instance attr-name]
  (update instance attr-name inc))

(defn hit-shortening [shortening]
  "Increment the hit count for the shortening and save the change to the DB."
  (save (inc-attr shortening :hit_count)))


