(ns gitcred.util)

(defn log [& message]
  (println (apply str message)))

(defn mash
  "Reduce a seq-able to a map. The given fn should return a 2-element tuple
  representing a key and value in the new map."
  [f coll]
  (reduce
    (fn [memo elem]
      (let [[k v] (f elem)]
        (assoc memo k v)))
    {} coll))

(defn update [m k f & args]
  "Like update-in, but without nesting."
  (assoc m k (apply f (get m k) args)))

(defn high
  "Like max, but for collections."
  [vals]
  (apply max vals))

(defn low
  "Like min, but for collections."
  [vals]
  (apply min vals))
