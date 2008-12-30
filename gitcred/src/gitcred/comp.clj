(ns gitcred.comp
  (:use (gitcred data utils))
  (:import (edu.uci.ics.jung.graph.impl
             DirectedSparseGraph DirectedSparseVertex DirectedSparseEdge)
           edu.uci.ics.jung.utils.UserData
           edu.uci.ics.jung.algorithms.importance.PageRank))

;(defn print-pagerank
;  []
;  (log "finding users, building verticies")
;  (let [users (all-users)
;        g     (DirectedSparseGraph.)
;        v-map (mash (fn [user]
;                      [(:username user)
;                       (doto (DirectedSparseVertex.)
;                             (.addUserDatum :user user UserData/SHARED))])
;                    users)]
;    (doseq [v (vals v-map)]
;      (.addVertex g v))
;    (log "finding follows, building edges")
;    (doseq [user users]
;      (let [from-v  (v-map (:username user))
;            follows (user-follows user)]
;        (doseq [follow follows]
;          (let [to-v (v-map (:to_username follow))]
;            (.addEdge g (DirectedSparseEdge. from-v to-v))))))
;    (log "computing pagerank")
;    (let [ranker (PageRank. g 0.15)]
;      (.evaluate ranker)
;      (log "printing rankings")
;      (let [rankings        (.getRankings ranker)
;            sorted-rankings (sort-by (fn [r] (.rankScore r)) rankings)]
;        (doseq [ranking sorted-rankings]
;          (let [user     (.getUserDatum (.vertex ranking) :user)
;                username (:username user)
;                score    (.rankScore ranking)]
;            (log (format "%s: %s" username score))))))))

; user => followings
; user => verticies
(defn compute-pagerank
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
      (log "printing rankings")
      (let [rankings        (.getRankings ranker)
            sorted-rankings (sort-by (fn [r] (.rankScore r)) rankings)]
        (doseq [ranking sorted-rankings]
          (let [user     (.getUserDatum (.vertex ranking) :user)
                username (:username user)
                score    (.rankScore ranking)]
            (log (format "%s: %s" username score))))))))