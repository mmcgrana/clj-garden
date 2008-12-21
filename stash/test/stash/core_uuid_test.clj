(in-ns 'stash.core-test)

(deftest "gen-uuid: returns a wellformed uuid string"
  (assert-matches +uuid-re+ (gen-uuid)))
