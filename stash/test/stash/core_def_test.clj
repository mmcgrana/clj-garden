(in-ns 'stash.core-test)

(defmodel +macro-post+ +post-map+)

(deftest "defmodel works"
  (assert= +macro-post+ (compiled-model +post-map+)))

(deftest "defmodel throws on unrecognized callback names"
  (assert-throws "wat"
    (compiled-model (assoc +post-map+ :callbacks {:before-update [:foobar]}))))

; quite a bit more to test here, not terrible interesting