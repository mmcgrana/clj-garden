(ns cljurl.models
  (:use (stash core timestamps validators)
        cljurl.utils)
  (:require [cljurl.config :as config]))


(def +slug-chars+  [\a \b \c \d \e \f \g \h \i \j \k \1 \2 \3 \4 \5])
(def +slug-length+ 5)

(defn generate-slug
  "Callbacks returning an instance of the shortening with its new slug."
  [shortening]
  (let [slug (str-cat (take +slug-length+ (map choice (repeat +slug-chars+))))]
    [(assoc shortening :slug slug) true]))

(defmodel +shortening+
  {:data-source config/data-source
   :table-name  :shortenings
   :pk-init a-uuid
   :columns
     [[:id         :uuid     {:pk true}]
      [:slug       :string   {:unique true}]
      [:url        :string]
      [:created_at :datetime]]
   :accessible-attrs
     [:url]
   :validations
     [[:url valid-url]]
   :callbacks
     {:before-create [timestamp-create generate-slug]}})

(defn find-shortening
  "Find the shortening based on a slug."
  [slug]
  (find-one +shortening+ {:where [:slug := slug]}))

(defn find-recent-shortenings
  "Returns the n most recently created shortenings."
  [n]
  (find-all +shortening+ {:limit n :order [:created_at :desc]}))

(defmodel +hit+
  {:data-source config/data-source
   :table-name :hits
   :pk-init a-uuid
   :columns
     [[:id            :uuid     {:pk true}]
      [:shortening_id :uuid]
      [:ip            :string]
      [:created_at    :datetime]
      [:updated_at    :datetime]
      [:hit_count     :integer]]
   :callbacks
     {:before-create [timestamp-create]
      :before-update [timestamp-update]}})

(defn hit-shortening [shortening ip]
  "Increment the hit count for the shortening and save the change to the DB."
  (if-let [hit (find-one +hit+
                 {:where [:and [:shortening_id := (:id shortening)]
                               [:ip := ip]]})]
    (save (inc-attr hit :hit_count))
    (create* +hit+ {:shortening_id (:id shortening) :ip ip :hit_count 1})))
