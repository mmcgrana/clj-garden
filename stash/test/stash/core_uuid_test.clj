(in-ns 'stash.core-test)

(deftest "gen-uuid: returns a wellformed uuid string"
  (assert-match +uuid-re+ (gen-uuid)))
