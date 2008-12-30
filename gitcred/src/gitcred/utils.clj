(ns gitcred.utils)

(defn log [message]
  (println message))

(defn mash
  "Reduce a seq-able to a map. The given fn should return a 2-element tuple
  representing a key and value in the new map."
  [f coll]
  (reduce
    (fn [memo elem]
      (let [[k v] (f elem)]
        (assoc memo k v)))
    {} coll))