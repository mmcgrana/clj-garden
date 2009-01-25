(ns cling.utils)

(defn high
  [coll]
  (and coll (last (sort coll))))

(defn compact
  [coll]
  (filter #(not (= % nil)) coll))