(ns stash.utils)

(defn update-by
  "Returns a new version of the given map with the value at the given key equal
  to invoking f on its old value.
  Like clojure.core/update-in, but less ambitious."
  [map key f]
  (assoc map key
    (f (get map key))))

(defn mash
  "Reduce a seq-able to a map. The given fn should return a 2-element tuple
  representing a key and value in the new map."
  [f coll]
  (reduce (fn [memo elem] (conj memo (f elem))) {} coll))

(defn with-assoc-meta
  "Returns an object with the key and value assoced onto its meta data."
  [obj k v]
  (with-meta obj (assoc (meta obj) k v)))

(defmacro def-
  "Like def, but creates a private var."
  [sym form]
  `(def ~(with-assoc-meta sym :private true) ~form))

(defmacro get-or
  "Short for (or (get map key) or-form)."
  [map key or-form]
  `(or (get ~map ~key) ~or-form))
