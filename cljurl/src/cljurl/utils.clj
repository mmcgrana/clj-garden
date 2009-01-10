(ns cljurl.utils
  (:use clojure.contrib.except))

(defmacro returning
  [val-form & body]
  `(let [return# ~val-form]
     ~@body
     return#))

(defmacro realtime
  [expr]
  `(let [start# (System/currentTimeMillis)]
     ~expr
     (- (System/currentTimeMillis) start#)))

(defmacro realtimed
  [expr]
  `(let [start# (System/currentTimeMillis)
         ret#    ~expr
         time#  (- (System/currentTimeMillis) start#)]
     [ret# time#]))

(defmacro with-realtime
  [[binding-sym expr] form]
  `(let [[ret# ~binding-sym] (realtimed ~expr)]
     ~form
     ret#))

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
