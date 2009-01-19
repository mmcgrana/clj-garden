(in-ns 'stash.core-test)

(defmodel +macro-post+ +post-map+)

(deftest "compiled-model: throws on unrecognized callback names"
  (assert-throws #"Unrecognized callback names: \(:foobar\)"
    (compiled-model (assoc +post-map+ :callbacks {:foobar [identity]}))))

(deftest "compiled-model: throws on unrecognized column types"
  (assert-throws #"Unrecognized column type: :bogus_type"
    (compiled-model (assoc-by +post-map+ :columns conj [:a_name :bogus_type]))))
