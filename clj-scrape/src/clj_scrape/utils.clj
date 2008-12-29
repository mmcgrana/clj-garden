(ns clj-scrape.utils)

(defn re-match?
  "Returns true iff the given string contains a match for the given pattern."
  [#^java.util.regex.Pattern pattern string]
  (.find (.matcher pattern string)))

(defn pattern?
  "Returns true if x is a Java Pattern."
  [x]
  (instance? java.util.regex.Pattern x))