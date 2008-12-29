(ns gitcred.data
  (:use stash.core clj-http-client.core clj-time.core))

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "gitcred")
    (.setUser         "mmcgrana")
    (.setPassword     "")))

(defmodel +user+
  {:data-source +data-source+
   :table-name :users
   :columns
     [[:username        :string]
      [:discovered_at   :datetime]
      [:last_scraped_at :datetime]]})

(defmodel +follow+
  {:data-source +data-source+
   :table-name :follows
   :columns
     [[:from_username :string]
      [:to_username   :string]]})

(defn user-url
  "Returns the github url for the user."
  [user]
  (str "http://github.com/" (:username user)))

(defn pull-user-page
  "Returns an html page for the user."
  [user]
  (let [[status headers body] (http-get (user-url user))]
    body))

(defn scrape-usernames
  "Given an html page, returns usernames of all people that the corresponding
  user follows."
  [page]
  (xml1-> (dom page) desc (id= "followers") :a (attr :title)))

(defn ensure-usernames
  "Ensure that we have users for all usernames, creating such users if we dont."
  [usernames]
  (doseq [username usernames]
    (when-not (find-one +user+ {:where [:username = username]})
      (create* +user+
        {:username username :discovered_at (now) :last_scraped_at nil}))))

(defn allign-follows
  [from-username to-usernames]
  (let [existing-follows
          (find-all +follow+
            {:where [:from_username := from-username]})
        existing-to-usernames
          (map :to_username existing-follows)
        needed-to-usernames
          (difference to-usernames existing-to-usernames)
        extra-to-usernames
          (difference existing-to-usernames to-usernames)]
    (doseq [needed-to-username needed-to-usernames]
      (create* +follow+
        {:from_username from-username :to_username to-username}))))

(defn update-scraped-at
  "Update the last_scraped_at time for the from-user to now and save."
  [from-user]
  (save (assoc from-user :last_scraped_at (now))))

(defn scrape1
  "Scrape 1 user. Ensures that all followed users exist in the db, that the
  follows from the user are all reflected in the db, and updates the
  last_scraped_at time for the user."
  [from-user]
  (let [page         (pull-user-page from-user)
        to-usernames (scrape-usernames page)]
    (ensure-usernames to-usernames)
    (allign-follows from-user to-usernames)
    (update-scrated-at from-user)))

(defn keep-scraping?
  "Returns true if we should continue scraping, false if we should stop."
  []
  (< (count-all +follow+) 100))

(defn next-user
  "Returns the next user from which we should scrape1."
  []
  (or (find-one +user+ {:where [:last_scraped_at := nil]
                        :order [:discovered_at :asc]})
      (find-one +user+ {:where [:last_scraped_at :not= nil]
                        :order [:last_scraped_at :asc]})))

(defn scrape
  "Breadth-first scrape the users/follows graph while keep-scraping?."
  [from-user]
  (loop [from-user from-user]
    (if (keep-scraping?)
      (scrape1 from-user)
      (recur (next-user)))))