(ns gitcred.view
  (:use (gitcred util))
  (:require (clojure.contrib [duck-streams :as ds])
            (gitcred [comp :as comp])))

(defn scaler
  "Returns a fn that logarithmicaly scales a pagerank r to a number
  between 0 or 10 based on the given lowest and highest possible pageranks."
  [l h]
  (fn [r]
    (* 10 (/ (- (Math/log r) (Math/log l))
             (- (Math/log h) (Math/log l))))))

(defn print-results
  "For given graph as returned by e.g. gitcred.data/all-graph-data, prints the
  normalized, sorted gitcred results to a file at results-path."
  [follows results-path]
  (let [results    (comp/compute-pagerank follows)
        name-width (high (map #(.length (first %)) results))
        format-str (str "%-" name-width "s %.2f\n")
        min-rank   (low  (map second results))
        max-rank   (high (map second results))
        scale-fn   (scaler min-rank max-rank)]
    (println "printing results")
    (with-open [w (ds/writer results-path)]
      (doseq [[user score] results]
        (let [gitcred (scale-fn score)]
          (.print w (format format-str user gitcred)))))))