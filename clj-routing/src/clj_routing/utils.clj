(ns clj-routing.utils)

(defn map-str
  "Like the usual map, but joins the resulting seq of strings together."
  [f coll]
  (apply str (map f coll)))

(defn mash
  "Reduce a seq-able to a map. The given fn should return a 2-element tuple
  representing a key and value in the new map."
  [f coll]
  (reduce
    (fn [memo elem]
      (let [[k v] (f elem)]
        (assoc memo k v)))
    {} coll))

(defn mash-grouped
  "Like mash, but groups values with the same key into a vector, preserving
  the order in which the values appreared in the original collection."
  [f coll]
  (reduce
    (fn [memo elem]
      (let [[k v] (f elem)]
        (assoc memo k (conj (get memo k []) v))))
    {}
    coll))

(defmacro get-or
  "Short for (or (get map key) or-form), used like (get m k else) where else
  needs to be lazy."
  [map key or-form]
  `(or (get ~map ~key) ~or-form))