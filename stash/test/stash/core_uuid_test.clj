(in-ns 'stash.core-test)

(deftest "gen-uuid"
  (assert-match +uuid-re+ (gen-uuid)))

(deftest "a-uuid"
  (let [m (a-uuid)]
    (assert= [:id] (keys m))
    (assert-match +uuid-re+ (m :id))))
