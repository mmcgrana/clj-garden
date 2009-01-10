(ns weldblog.models
  (:use (stash core validators))
  (:require [weldblog.config :as config]))

(def model-base
  {:data-source config/data-source
   :logger      config/logger})

(defmodel +post+
  (merge model-base
  {:table-name :posts
   :pk-init a-uuid
   :columns
     [[:id    :uuid   {:pk true}]
      [:title :string]
      [:body  :string]]
   :accessible-attrs
     [:title :body]
   :validations
     [[:url presence]]}))