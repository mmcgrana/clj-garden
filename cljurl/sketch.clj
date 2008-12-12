(path generator :index)
(path generator :foo {:id "biz"})

(declare gen-slug)

(ns cljurl.app.models
  (:require [stash.core :as orm]))

(def shortening
  (stash.models/define
    {:properties
      [[:slug :string {:width 12}]
       [:url  :string {:width 1000}]]
     :validations
       [[:slug :presence]
        [:url :format {:with +url-regexp+}]]
     :callbacks
       {:before-create gen-slug}}))

(defn- gen-slug
  [instance]
  (assoc instance :slug (next-slug)))

(defn- next-slug
  "Returns the next available short slug after the given slug."
  [prev-slug]
  (str (gensym "slug")))

(defn find-shortening-by-slug
  "Returns the shortening corresponding to the given slug, or nil if no such
  shortening exists."
  [slug]
  (orm/one shortening {:where [:= :slug slug]}))

(defn find-shortening-by-url
  "Returns the shortening corresponding to the given url, or nil if no such
  shortening exists."
  [url]
  (orm/one shortening {:where [:= :url url]}))

(defn find-prev-slug
  "Returns the last slug in lexographic order."
  []
  (orm/maximum shortening :slug))

(defn create-shortening
  [attrs]
  (orm/create shortening attrs))

(defn find-or-create-shortening-by-url
  "Return a shortenging representing a new or preferablly existing shortening of 
  the given url."
  [url]
  (or (orm/one shortening {:where [:= :url url]})
      (orm/create shortening {:url url})))