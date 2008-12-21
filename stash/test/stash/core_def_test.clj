(in-ns 'stash.core-test)

(defmodel +macro-post+ +post-map+)

(deftest "defmodel"
  (assert= +macro-post+ (compiled-model +post-map+)))

(deftest "defmodel: throws on unrecognized callback names"
  (assert-throws "Unrecognized keys (:foobar)"
    (compiled-model (assoc +post-map+ :callbacks {:foobar [identity]}))))

; TODO: test rest