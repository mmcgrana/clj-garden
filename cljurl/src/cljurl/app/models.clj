(ns cljurl.app.models
  (:use    stash.core
           stash.timestamps [stash.validators :only (valid-url)]
           cljurl.utils)
  (:require cljurl.config))


(def +slug-chars+  [\a \b \c \d \e \f \g \h \i \j \k \1 \2 \3 \4 \5])
(def +slug-length+ 5)

(defn generate-slug
  "Callbacks returning an instance of the shortening with its new slug."
  [shortening]
  (let [slug (str-cat (take +slug-length+ (map choice (repeat +slug-chars+))))]
    [(assoc shortening :slug slug) true]))


(defmodel +shortening+
  {:data-source cljurl.config/+data-source+
   :table-name  :shortenings
   :columns
     [[:slug       :string]
      [:url        :string]
      [:created_at :datetime]]
   :validations
     [[:url valid-url]]
   :callbacks
     {:before-create [timestamp-create generate-slug]}})


(defn find-recent-shortenings
  "Returns the n most recently created shortenings."
  [n]
  (find-all +shortening+ {:limit n :order [:created_at :desc]}))

