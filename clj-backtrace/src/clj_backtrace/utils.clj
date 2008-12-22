(ns clj-backtrace.utils
  (:use clojure.contrib.str-utils))

(defn mash
  "Reduce a seq-able to a map. The given fn should return a 2-element tuple
  representing a key and value in the new map."
  [f coll]
  (reduce
    (fn [memo elem]
      (let [[k v] (f elem)]
        (assoc memo k v)))
    {} coll))

(defn re-without
  "Returns a String with the given pattern re-gsub'd out the given string."
  [pattern string]
  (re-gsub pattern "" string))

(defn re-matches?
  "Returns true iff the given string contains a match for the given pattern."
  [#^java.util.regex.Pattern pattern string]
  (.find (.matcher pattern string)))

(defn re-get
  "Returns the nth captured group resulting from matching the given pattern
  against the given string, or nil if no match is found."
  [re s n]
  (let [m (re-matcher re s)]
    (if (.find m)
      (.group m n))))