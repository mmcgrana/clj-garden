(ns cling.models
  (:use
    (stash core validators timestamps)
    (clojure.contrib str-utils))
  (:require
    (cling [config :as config])))

(def model-base
  {:data-source config/data-source
   :logger      config/logger})

(declare add-permalink)

(defmodel +page+
  {:data-source config/data-source
   :logger      config/logger
   :table-name  :pages
   :columns
     [[:id :uuid {:pk true :auto true}]
      [:title :string]
      [:permalink :string]
      [:body  :string]
      [:created_at :datetime]
      [:updated_at :datetime]]
   :accessible-attrs
     [:title :body]
   :callbacks
      {:before-create [timestamp-create #'add-permalink]
       :before-save   [timestamp-update]}})

(defn format-permalink
  [title]
  (let [p (.toLowerCase title)
        p (re-gsub #"[^a-z0-9]" "-" p)
        p (re-gsub #"-{2,}" "-" p)
        p (re-gsub #"(^-)|(-$)" "" p)]
    p))

(defn add-permalink
  [page]
  [(assoc page :permalink (format-permalink (:title page))) true])

(defn find-page-by-permalink
  [permalink]
  (find-one +page+ {:where [:permalink := permalink]}))
