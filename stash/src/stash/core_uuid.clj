(in-ns 'stash.core)

(defn gen-uuid
  "Returns a String corresponding to a random new UUID."
  []
  (str (java.util.UUID/randomUUID)))