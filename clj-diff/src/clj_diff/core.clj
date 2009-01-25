(ns clj-diff.core
  (:use
    (clojure.contrib str-utils))
  (:import
    (org.incava.util.diff Diff Difference)))

(defn- num-unless-neg [x]
  (if-not (neg? x) x))

(defn- dtuple [d]
  [(num-unless-neg (.getDeletedStart d))
   (num-unless-neg (.getDeletedEnd   d))
   (num-unless-neg (.getAddedStart   d))
   (num-unless-neg (.getAddedEnd     d))])

(defn diff [coll-a coll-b]
  "Returns the difference tuples for the diff from coll-a to coll-b."
  (map dtuple (.diff (Diff. coll-a coll-b))))

(defn diff-text [text-a text-b]
  "Returns a 3-tuple: a vector of the lines from text-a, a vector of the lines
  from text-b, and the difference tuples for diff from the a lines to the
  b lines."
  (let [lines-a     (vec (re-split #"\n" text-a))
        lines-b     (vec (re-split #"\n" text-b))]
    [lines-a lines-b (diff lines-a lines-b)]))

; TODO: coallesced context (can skip for now)
; TODO: line isn't quite right for cases with upper context
; TODO: seperate parsing from printing

(defn print-pair [left right]
  (printf "%-8s%-8s\n" (or left "") (or right "")))

(defn sub-lines [lines start end]
  (subvec lines start (inc end)))

(defn print-pairs [lefts rights]
  (doseq [i (range (max (count lefts) (count rights)))]
    (print-pair (if-let [left  (get lefts i)]  (str "- " left))
                (if-let [right (get rights i)] (str "+ " right)))))

(defn print-hunk [[start-a end-a start-b end-b] lines-a lines-b]
  (println)
  (print-pair (str "Line " (if (> start-a 0) start-a 1) ":")
              (str "Line " (if (> start-b 0) start-b 1) ":"))
  (let [line-a (and (> start-a 0) (nth lines-a (dec start-a)))
        line-b (and (> start-b 0) (nth lines-b (dec start-b)))]
    (if (or line-a line-b)
      (print-pair (if line-a (str "* " line-a)) (if line-b (str "* " line-b)))))
  (cond (and end-a end-b)
          (print-pairs (sub-lines lines-a start-a end-a)
                       (sub-lines lines-b start-b end-b))
        end-a
          (print-pairs (sub-lines lines-a start-a end-a) nil)
        end-b
          (print-pairs nil (sub-lines lines-b start-b end-b)))
  (let [end-a* (or end-a (dec start-a))
        end-b* (or end-a (dec start-a))
        line-a (and (< (inc end-a*) (count lines-a)) (nth lines-a (inc end-a*)))
        line-b (and (< (inc end-b*) (count lines-b)) (nth lines-b (inc end-b*)))]
    (if (or line-a line-b)
      (print-pair (if line-a (str "* " line-a)) (if line-b (str "* " line-b))))))

(defn print-diff-files [path-a path-b]
  (let [[lines-a lines-b dtuples] (diff-text (slurp path-a) (slurp path-b))
        cdiff                     (column-diff lines-a lines-b dtuples)]
    (prn dtuples)
    (prn cdiff)
    (doseq [dtuple dtuples]
      (print-hunk dtuple lines-a lines-b))))

