(ns cling.utils)

(defn high
  [coll]
  (and coll (last (sort coll))))