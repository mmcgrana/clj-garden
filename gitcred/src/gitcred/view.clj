(ns gitcred.view
  (:use (gitcred data comp utils) clojure.contrib.duck-streams))

(defn scaler
  "Returns a fn that logarithmicaly scales a pagerank probability to a number
  less than or equal to ceiling, where a probility of max-prob maps to ceiling."
  [base ceiling max-prob]
  (let [scale    (* (/ 1 max-prob) (Math/pow base ceiling))
        log-base (Math/log base)
        log      (fn [x] (/ (Math/log x) log-base))]
    (fn [x] (log (* x scale)))))

(defn print-results
  "For given graph as returned by e.g. gitcred.data/all-graph-data, prints the
  normalized, sorted gitcred results to a file at results-path."
  [users-to-followers results-path]
  (let [results    (compute-pagerank users-to-followers)
        name-width (high (map #(.length (:username (first %))) results))
        format-str (str "%-" name-width "s %.2f\n")
        scale-fn   (scaler 1.9 10 (second (first results)))]
    (log (str "printing results to " results-path))
    (with-open [w (writer results-path)]
      (doseq [result results]
        (let [username (:username (first result))
              gitcred  (scale-fn (second result))]
          (.print w (format format-str username gitcred)))))))