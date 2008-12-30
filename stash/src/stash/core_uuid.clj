(in-ns 'stash.core)

(defn gen-uuid
  "Returns a String corresponding to a random new UUID."
  []
  (str (java.util.UUID/randomUUID)))

(defn a-uuid
  "Returns a hash with :id going to a newly generate uuid, per gen-uuid."
  []
  {:id (gen-uuid)})