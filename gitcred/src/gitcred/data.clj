(ns gitcred.data
  (:use (gitcred util))
  (:require (clojure.contrib.http [agent :as http])
            (fleetdb [embedded :as embedded])))

(defn- parse-follow-list [string]
  ((read-string (.replace string ":" ",")) "users"))

(defn- follow-list-url [follow-type user]
  (str "http://github.com/api/v2/json/user/show/" user "/" follow-type))

(defn- fetch-follow-list [follow-type user]
  "Where follow-type is one of followers or following, returns the corresponding
   array of usernames."
  (let [agent   (http/http-agent (follow-list-url follow-type user))
        body    (http/string agent)
        status  (http/status agent)
        message (http/message agent)]
    (cond
      (= status 200)
        (parse-follow-list body)
      (= status 403)
        (do (println "Limit exceeded, retrying after 60 seconds\n")
            (Thread/sleep 60000)
            (fetch-follow-list follow-type user))
      (= status 404)
        (println "User not found; skipping")
      :unexpected
        (do (println "Unexpected Error!")
            (println status)
            (println message)
            (println body)
            (System/exit 1)))))

(defn- ensure-user
  "Ensures that we have a user record for the given username."
  [dba user]
  (when-not (embedded/query dba [:get :users user])
    (embedded/query dba
      [:insert :users {:id user :found_at (System/currentTimeMillis)}])))

(defn- ensure-follow
  "Ensure that we have a follow record for users from to to."
  [dba from to]
  (let [follow-id (str from "->" to)]
    (when-not (embedded/query dba [:get :follows follow-id])
      (embedded/query dba
        [:insert :follows {:id follow-id :from from :to to}]))))

(defn- count-unfetched-users
  "Returns the number of users that have not yet been fetched."
  [dba]
  (embedded/query dba [:count :users {:where [:= :fetched_at nil]}]))

(defn- next-user
  "Returns the next user for which we should fetch."
  [dba]
  (first (embedded/query dba
           [:select :users {:where [:= :fetched_at nil]
                            :order [[:found_at :asc]]
                            :limit 1
                            :only :id}])))

(defn- mark-fetch
  "Mark the user as having been fetched for follows."
  [dba user]
  (embedded/query dba
    [:update :users {:fetched_at (System/currentTimeMillis)}
                    {:where [:= :id user]}]))

(defn- fetch
  "Fetch data for one user. Ensures that all followed and following users exist
  in the db and that the corresponding follows are recorded. Marks the user
  as fetched."
  [dba user]
  (println "fetching" user)
  (let [followers (fetch-follow-list "followers" user)
        followeds (fetch-follow-list "following" user)]
    (println (count followers) "followers," (count followeds) "followed")
    (doseq [follow (concat followers followeds)]
      (ensure-user dba follow))
    (doseq [follower followers]
      (ensure-follow dba follower user))
    (doseq [followed followeds]
      (ensure-follow dba user followed))
    (mark-fetch dba user)))

(defn- fetch-from
  "Fetch the users/follows graph starting at from-user."
  [dba initial-user]
  (loop [from-user initial-user]
    (let [left (count-unfetched-users dba)]
      (if (> left 0)
        (do
          (fetch dba from-user)
          (println left "users unfetched\n")
          (Thread/sleep 2000)
          (recur (next-user dba)))
        (println "done fetching")))))

(defn fetch-graph-data
  "Run the fetcher."
  [dba]
  (ensure-user dba "mmcgrana")
  (fetch-from dba (next-user dba)))

(defn follows-data
  "Returns a collection of [from to] vectors corresponding to follow edges
   on the users/follows graph."
  [dba]
  (println "compiling graph data")
  (embedded/query dba [:select :follows {:only [:from :to]}]))

(defn ensure-indexes
  [dba]
  (embedded/query dba [:create-index :users [[:found_at :asc]]])
  (embedded/query dba [:create-index :follows [[:to :asc]]]))
