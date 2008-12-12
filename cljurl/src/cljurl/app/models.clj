(ns cljurl.app.models
  (:require [cljurl.sql :as sql]))

(def start-char-cycle \a)
(def end-char-cycle   \d)
(def char-cycle {\a \b, \b \c, \c \d})

(defn- next-slug
  "Returns the next available short slug after the given slug."
  [prev-slug]
  (let [prev-length (.length prev-slug)
        prev-last   (.charAt prev-slug (dec prev-length))
        prev-front  (.substring prev-slug 0 (dec prev-length))]
    (if (= prev-last end-char-cycle)
      (str prev-slug start-char-cycle)
      (str prev-front (char-cycle prev-last)))))

(defn find-shortening-by-slug
  "Returns the shortening corresponding to the given slug, or nil if no such
  shortening exists."
  [slug]
  (sql/select-map "SELECT * FROM shortenings WHERE slug = ?" slug))

(defn find-shortening-by-url
  "Returns the shortening corresponding to the given url, or nil if no such
  shortening exists."
  [url]
  (sql/select-map "SELECT * FROM shortenings WHERE url = ?" url))

(defn find-prev-slug
  "Returns the last slug in lexographic order."
  []
  (sql/select-value "SELECT slug FROM shortenings ORDER BY slug DESC LIMIT 1"))

(defn create-shortening
  [{:keys [url slug] :as shortening}]
  (sql/modify "INSERT INTO shortenings (url, slug) VALUES (?, ?)" url slug)
  shortening)

(defn create-shortening-by-url
  "Create a shortening for the given url."
  [url]
  (let [prev-slug  (sql/select-value "SELECT slug FROM shortenings ORDER BY slug DESC")
        slug       (next-slug prev-slug)]
    (create-shortening {:url url :slug slug})))

(defn find-or-create-shortening-by-url
  "Return a shortenging representing a new or preferablly existing shortening of 
  the given url."
  [url]
  (or (find-shortening-by-url url) (create-shortening-by-url url)))

(defn valid-shortening?)
