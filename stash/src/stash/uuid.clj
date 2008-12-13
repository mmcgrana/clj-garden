(ns stash.uuid
  (:import (java.util UUID)))

(defn gen
  "Returns a random new UUID instance."
  []
  (UUID/randomUUID))