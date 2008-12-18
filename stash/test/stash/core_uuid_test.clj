(in-ns 'stash.core-test)

(deftest "gen-uuid returns a wellformed uuid string"
  (assert-matches
    #"[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}"
    (gen-uuid)))
