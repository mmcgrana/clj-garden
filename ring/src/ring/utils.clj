(ns ring.utils
  (:use clojure.contrib.str-utils))

(defn re-without
  "Returns a String with the given pattern re-gsub'd out the given string."
  [pattern string]
  (re-gsub pattern "" string))

(defn re-match?
  "Returns true iff the given string contains a match for the given pattern."
  [#^java.util.regex.Pattern pattern string]
  (.find (.matcher pattern string)))

(defn take-last
  "Returns a seq of the n last items in the seq-able coll. If n is greater than
  the count of coll, returns a seq of coll."
  [n coll]
  (let [c (count coll)]
    (drop (- c n) coll)))

(defn update
  "'Updates' a value in an associative structure, where k is a key and f is a 
  function that will take the old value and any supplied args and return the new 
  value, and returns the new associative structure."
  [m k f & args]
  (assoc m k (apply f (get m k) args)))

(defn str-cat
  "Concat the given strings into a single string. Like (str-join \"\" strs)."
  [strs]
  (apply str strs))
