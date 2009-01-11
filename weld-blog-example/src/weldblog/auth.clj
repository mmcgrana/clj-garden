(ns weldblog.auth)

(def auth-key :authenticated)

(defn authenticated?
  [sess]
  (get sess auth-key))

(defn authenticated
  [sess]
  (assoc sess auth-key true))

(defn unauthenticated
  [sess]
  (dissoc sess auth-key))