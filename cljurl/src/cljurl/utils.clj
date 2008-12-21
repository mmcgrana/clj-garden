(ns cljurl.utils)

(defn str-cat
  "Concat the given strings into a single string. Like (str-join \"\" strs)."
  [strs]
  (apply str strs))

(defn update
  "'Updates' a value in an associative structure, where k is a key and f is a 
  function that will take the old value and any supplied args and return the new 
  value, and returns the new associative structure."
  [m k f & args]
  (assoc m k (apply f (get m k) args)))

(defn choice
  "Returns a random element from the given vector."
  [#^clojure.lang.IPersistentVector vec]
  (get vec (rand-int (count vec))))
