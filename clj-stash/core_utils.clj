(in-ns 'clj-http-client.core)

(defn mash
  "Reduce a collection to a map. The given f should return a [key value] pair
  for the passed element in the collection."
  [f coll]
  (reduce
    (fn [map elem]
      (let [[k v] (f elem)]
        (assoc map k v)))
    {}
    coll))