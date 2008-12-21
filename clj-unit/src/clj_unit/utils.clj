(ns clj-unit.utils)

(defn update
  "'Updates' a value in an associative structure, where k is a key and f is a 
  function that will take the old value and any supplied args and return the new 
  value, and returns the new associative structure."
  [m k f & args]
  (assoc m k (apply f (get m k) args)))