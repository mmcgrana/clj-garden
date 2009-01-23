(ns weldblog.auth
  (require
    [weldblog.config :as config]))

(def auth-key :authenticated)

(defn authenticate?
  [pass]
  (= config/admin-password pass))

(defn authenticated?
  [sess]
  (get sess auth-key))

(defn authenticated
  [sess]
  (assoc sess auth-key true))

(defn unauthenticated
  [sess]
  (dissoc sess auth-key))