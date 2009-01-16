(in-ns 'stash.core-test)

(deftest "auto-uuid"
  (assert-match +uuid-re+ (auto-uuid)))
