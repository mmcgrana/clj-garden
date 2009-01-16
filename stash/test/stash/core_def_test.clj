(in-ns 'stash.core-test)

(defmodel +macro-post+ +post-map+)

(deftest "instance-model: no model"
  (assert-throws #"No model found" (instance-model {})))

(deftest "instance-model: model"
  (assert= +post+ (instance-model (init +post+))))

(deftest "compiled-model: throws on unrecognized callback names"
  (assert-throws #"Unrecognized callback names: \(:foobar\)"
    (compiled-model (assoc +post-map+ :callbacks {:foobar [identity]}))))

(deftest "compiled-model: throws on unrecognized column types"
  (assert-throws #"Unrecognized column type: :bogus_type"
    (compiled-model (assoc-by +post-map+ :columns conj [:a_name :bogus_type]))))

(deftest "defmodel"
  (assert= +macro-post+ (compiled-model +post-map+)))

(deftest "column-names"
  (assert= [:id :title :view_count :posted_at :special]
    (column-names +post+))
  (assert=   [:pk_uuid :pk_integer :a_uuid :a_integer :a_boolean :a_long
              :a_float :a_double :a_string :a_datetime]
     (column-names +schmorg+)))

(deftest "pk-column-names"
  (assert= [:id] (pk-column-names +post+))
  (assert= [:pk_uuid :pk_integer] (pk-column-names +schmorg+)))

(deftest "non-pk-column-names"
  (assert= [:title :view_count :posted_at :special]
    (non-pk-column-names +post+))
  (assert=
    [:a_uuid :a_integer :a_boolean :a_long :a_float :a_double :a_string  :a_datetime]
    (non-pk-column-names +schmorg+)))

; TODO: test model accessors

