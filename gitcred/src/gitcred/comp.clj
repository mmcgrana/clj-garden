(ns gitcred.comp
  (:import (edu.uci.ics.jung.graph DirectedSparseGraph)
           (edu.uci.ics.jung.graph.util EdgeType)
           (edu.uci.ics.jung.algorithms.scoring PageRank)))

(defn compute-pagerank
  "For given graph as returned by e.g. gitcred.data/all-graph-data, returns
  a seq of 2-tuples, where the first element is the username and the second is
  the users' un-normalized pagerank, sorted by pagerank."
  [follows]
  (println "building graph")
  (let [graph     (DirectedSparseGraph.)]
    (doseq [[from to :as pair] follows]
      (.addVertex graph from)
      (.addVertex graph to)
      (.addEdge graph pair from to EdgeType/DIRECTED))
    (println "computing pagerank")
    (let [ranker (PageRank. graph 0.15)]
      (.evaluate ranker)
      (let [rankings (map (fn [user] [user (.getVertexScore ranker user)])
                          (.getVertices graph))]
        (sort-by (fn [[user score]] (- score)) rankings)))))
