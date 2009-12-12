(ns gitcred.comp
  (:import (edu.uci.ics.jung.graph DirectedSparseGraph)
           (edu.uci.ics.jung.graph.util EdgeType)
           (edu.uci.ics.jung.algorithms.scoring PageRank)))

(defn compute-pagerank
  "For given graph as returned by e.g. gitcred.data/all-graph-data, returns
  a seq of 2-tuples, where the first element is the username and the second is
  the users' un-normalized pagerank, sorted by pagerank."
  [users-to-followers]
  (println "building graph")
  (let [graph     (DirectedSparseGraph.)
        users     (keys users-to-followers)]
    (doseq [user users]
      (.addVertex graph user))
    (doseq [[user followers] users-to-followers]
      (doseq [follower followers]
        (.addEdge graph [follower user] follower user EdgeType/DIRECTED)))
    (println "computing pagerank")
    (let [ranker (PageRank. graph 0.15)]
      (.evaluate ranker)
      (let [rankings (map #(vector % (.getVertexScore ranker %)) users)]
        (sort-by (fn [[user score]] (- score)) rankings)))))
