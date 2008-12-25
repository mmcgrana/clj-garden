(ns clj-unit.utils)

(defn update
  "'Updates' a value in an associative structure, where k is a key and f is a 
  function that will take the old value and any supplied args and return the new 
  value, and returns the new associative structure."
  [m k f & args]
  (assoc m k (apply f (get m k) args)))

(defn re-match?
  "Returns true iff the given string contains a match for the given pattern."
  [#^java.util.regex.Pattern pattern string]
  (.find (.matcher pattern string)))

(defmacro file-line
  "Expands into a [file, line] pair corresponding to the function in which the
  expansion occurs."
  []
  [(var-get clojure.lang.Compiler/SOURCE)
   (var-get clojure.lang.Compiler/LINE)])