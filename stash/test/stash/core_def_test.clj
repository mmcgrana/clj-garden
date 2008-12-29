(in-ns 'stash.core-test)

(defmodel +macro-post+ +post-map+)

(deftest "instance-model: no model"
  (assert-throws #"No model found"
    (instance-model {})))

(deftest "instance-model: model"
  (assert= +post+ (instance-model (init +post+))))

(deftest "defmodel"
  (assert= +macro-post+ (compiled-model +post-map+)))

(deftest "defmodel: throws on unrecognized callback names"
  (assert-throws #"Unrecognized keys \(:foobar\)"
    (compiled-model (assoc +post-map+ :callbacks {:foobar [identity]}))))

(defmodel +post-with-accessors+ +post-map+ {:accessors true})

(deftest "defmodel: with def-accessors"
  (assert-nil (title (init +post-with-accessors+)))
  (assert= "foo" (title (title= (init +post-with-accessors+) "foo")))
  (assert-not (title? (init +post-with-accessors+)))
  (assert-that (title? (title= (init +post-with-accessors+) "foo"))))

; TODO: test rest