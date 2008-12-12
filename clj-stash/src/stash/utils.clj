(ns stash.utils)

(defn with-updated-meta
  [obj new-meta]
  (with-meta obj (merge (meta obj new-meta))))