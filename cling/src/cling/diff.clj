(ns cling.diff
  (:use
    (clojure.contrib str-utils fcase)
    (cling utils))
  (:import
    (org.incava.util.diff Diff Difference)))

(defn- num-unless-neg
  [x]
  (if-not (neg? x) x))

(defn diff
  [coll-a coll-b]
  (for [d (.diff (Diff. coll-a coll-b))]
    [(num-unless-neg (.getDeletedStart d))
     (num-unless-neg (.getDeletedEnd   d))
     (num-unless-neg (.getAddedStart   d))
     (num-unless-neg (.getAddedEnd     d))]))

(def line-sep #"(\r\n)|(\r)|(\n)")

(defn diff-text
  [text-a text-b]
  (let [lines-a     (vec (re-split line-sep text-a))
        lines-b     (vec (re-split line-sep text-b))]
    [lines-a lines-b (diff lines-a lines-b)]))

(defn column-diff
  [elems-a elems-b dtuples]
  (for [[start-a end-a start-b end-b] dtuples]
    (let [[first-line-a top-context-a]
            (if (pos? start-a) [(dec start-a) (nth elems-a (dec start-a))] [0 nil])
          [first-line-b top-context-b]
            (if (pos? start-b) [(dec start-b) (nth elems-b (dec start-b))] [0 nil])
          top-context
            (if (or top-context-a top-context-b)
               [:context top-context-a top-context-b])
          bottom-context-a
            (get elems-a (or (and end-a (inc end-a)) start-a))
          bottom-context-b
            (get elems-b (or (and end-b (inc end-b)) start-b))
          bottom-context
            (if (or bottom-context-a bottom-context-b)
               [:context bottom-context-a bottom-context-b])
          change-elems-a
            (if end-a (subvec elems-a start-a (inc end-a)))
          change-elems-b
            (if end-b (subvec elems-b start-b (inc end-b)))
          change-sym
            (cond (and end-a end-b) :change end-a :deletion end-b :addition)
          change-elems
          (for [i (range (max (count change-elems-a) (count change-elems-b)))]
            [change-sym (get change-elems-a i) (get change-elems-b i)])]
      [[first-line-a first-line-b]
       (compact (cons top-context (concat change-elems (list bottom-context))))])))

(defn column-diff-text
  [text-a text-b]
  (let [[lines-a lines-b dtuples] (diff-text text-a text-b)]
    [lines-a lines-b (column-diff lines-a lines-b dtuples)]))
