(ns stash.uuid
  (:import (java.util UUID)))

(defn gen
  []
  (UUID/randomUUID))