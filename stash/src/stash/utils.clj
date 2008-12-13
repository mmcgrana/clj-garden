(ns stash.utils)

(defn with-updated-meta
  "Returns a new object corresponding to the given obj but the given new-meta
  merged into any existing meta."
  [obj new-meta]
  (with-meta obj (merge (meta obj new-meta))))

(defmacro def-)

(defn upcase
  [string]
  (.toUpperCase string))

(defn the-str
  [named]
  (.name named))