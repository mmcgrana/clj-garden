(ns gitcred.comp
  (:use (gitcred data utils))
  (:import (edu.uci.ics.jung.graph.impl
             DirectedSparseGraph DirectedSparseVertex DirectedSparseEdge)
           edu.uci.ics.jung.utils.UserData
           edu.uci.ics.jung.algorithms.importance.PageRank))

(defn compute-pagerank
  "For given graph as returned by e.g. gitcred.data/all-graph-data, returns
  a seq of 2-tuples, where the first element is the user and the second is
  the users' un-normalized pagerank, sorted by pagerank."
  [users-to-followers]
  (log "building graph")
  (let [users (keys users-to-followers)
        g     (DirectedSparseGraph.)
        v-map (mash (fn [user]
                      [(:username user)
                       (doto (DirectedSparseVertex.)
                             (.addUserDatum :user user UserData/SHARED))])
                    users)]
    (doseq [v (vals v-map)]
      (.addVertex g v))
    (doseq [user users]
      (let [from-v  (v-map (:username user))
            follows (users-to-followers user)]
        (doseq [follow follows]
          (let [to-v (v-map (:to_username follow))]
            (.addEdge g (DirectedSparseEdge. from-v to-v))))))
    (log "computing pagerank")
    (let [ranker (PageRank. g 0.15)]
      (.evaluate ranker)
      (let [rankings        (.getRankings ranker)
            sorted-rankings (sort-by (fn [r] (- (.rankScore r))) rankings)]
        (map (fn [r] [(.getUserDatum (.vertex r) :user) (.rankScore r)])
             sorted-rankings)))))
