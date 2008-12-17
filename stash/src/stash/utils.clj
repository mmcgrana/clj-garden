(ns stash.utils)

(defn with-assoc-meta
  "Returns an object with the key and value assoced onto its meta data."
  [obj k v]
  (with-meta obj (assoc (meta obj) k v)))

(defmacro def-
  "Like def, but creates a private var."
  [sym form]
  `(def #^{:private true} ~sym form))

; (defn with-updated-meta
;   "Returns a new object corresponding to the given obj but the given new-meta
;   merged into any existing meta."
;   [obj new-meta]
;   (with-meta obj (merge (meta obj new-meta))))