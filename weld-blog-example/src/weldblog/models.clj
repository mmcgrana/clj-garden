(ns weldblog.models
  (:use (stash core validators timestamps))
  (:require [weldblog.config :as config]))

(def model-base
  {:data-source config/data-source
   :logger      config/logger})

(defmodel +post+
  (merge model-base
  {:table-name :posts
   :columns
     [[:id         :uuid     {:pk true :auto true}]
      [:title      :string]
      [:body       :string]
      [:created_at :datetime]
      [:updated_at :datetime]]
   :accessible-attrs
     [:title :body]
   :validations
     [[:title    presence]
      [:body     presence]]
   :callbacks
     {:before-create [timestamp-create]
      :before-save   [timestamp-update]}}))
