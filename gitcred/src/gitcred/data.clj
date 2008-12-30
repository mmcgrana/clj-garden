(ns gitcred.data
  (:use gitcred.utils
        stash.core
        clj-http-client.core clj-time.core clj-scrape.core
        clojure.set))

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "gitcred_dev")
    (.setUser         "mmcgrana")
    (.setPassword     "")))

(defmodel +user+
  {:data-source +data-source+
   :table-name :users
   :pk         :username
   :columns
     [[:username        :string]
      [:discovered_at   :datetime]
      [:last_scraped_at :datetime]]})

(defmodel +follow+
  {:data-source +data-source+
   :table-name :follows
   :pks        [:from_username :to_username]
   :columns
     [[:from_username :string]
      [:to_username   :string]]})

(defn user-url
  "Returns the github url for the user."
  [user]
  (str "http://github.com/" (:username user)))

(defn parse-usernames
  "Given an html stream, returns usernames of all people that the corresponding
  user follows."
  [stream]
  (xml-> (dom stream) desc {:class "followers"} desc :a (attr :title)))

(defn scrape-usernames
  "Returns an html page for the user."
  [user]
  (http-get-stream (user-url user) {}
    (fn [s h b-stream] (parse-usernames b-stream))))

(defn ensure-usernames
  "Ensure that we have users for all usernames, creating such users if we dont."
  [usernames]
  (doseq [username usernames]
    (when-not (find-one +user+ {:where [:username := username]})
      (create* +user+ {:username username :discovered_at (now)}))))

(defn allign-follows
  [from-user to-usernames]
  (let [to-usernames-set (set to-usernames)
        from-username    (:username from-user)
        existing-follows
          (find-all +follow+
            {:where [:from_username := from-username]})
        existing-to-usernames-set
          (set (map :to_username existing-follows))
        needed-to-usernames
          (difference to-usernames-set existing-to-usernames-set)
        extra-to-usernames
          (difference existing-to-usernames-set to-usernames-set)]
    (doseq [needed-to-username needed-to-usernames]
      (create* +follow+
        {:from_username from-username :to_username needed-to-username}))))

(defn update-scraped-at
  "Update the last_scraped_at time for the from-user to now and save."
  [from-user]
  (save (assoc from-user :last_scraped_at (now))))

(defn count-unscraped-users
  "Returns the number of unscraped users."
  []
  (count-all +user+ {:where [:last_scraped_at := nil]}))

(defn next-user
  "Returns the next user from which we should scrape1."
  []
  (or (find-one +user+ {:where [:last_scraped_at := nil]
                        :order [:discovered_at :asc]})
      (find-one +user+ {:where [:last_scraped_at :not= nil]
                        :order [:last_scraped_at :asc]})))

(defn scrape1
  "Scrape 1 user. Ensures that all followed users exist in the db, that the
  follows from the user are all reflected in the db, and updates the
  last_scraped_at time for the user."
  [from-user]
  (log (str "scraping username " (:username from-user)))
  (let [to-usernames (scrape-usernames from-user)]
    (log (str "follows " (count to-usernames) " users, ensuring"))
    (ensure-usernames to-usernames)
    (log "alligning followers")
    (allign-follows from-user to-usernames)
    (update-scraped-at from-user)))

(defn scrape
  "Breadth-first scrape the users/follows graph while keep-scraping?."
  [from-user]
  (loop [from-user from-user]
    (let [left (count-unscraped-users)]
      (if (> left 0)
        (do
          (scrape1 from-user)
          (log (str left " users unscraped\n"))
          (Thread/sleep 10000)
          (recur (next-user)))
        (log "done scraping")))))

(defn ensure-seed-user []
  (when-not (exist? +user+ {:where [:username := "mmcgrana"]})
    (create* +user+ {:username "mmcgrana" :discovered_at (now)})))

(defn run []
  (ensure-seed-user)
  (scrape (next-user)))


(defn all-graph-data []
  (log "loading all graph data")
  (mash
    (fn [user]
      [user (find-all +follow+ {:where [:from_username := (:username user)]})])
    (find-all +user+)))

(defn partial-graph-data []
  (log "loading partial graph data")
  (let [users     (find-all +user+ {:limit 500})
        usernames (map :username users)]
    (mash
      (fn [user]
        [user (find-all +follow+
                {:where [:and [:from_username := (:username user)]
                              [:to_username :in usernames]]})])
      users)))
