(in-ns 'stash.core-test)

(defn presence
  [attr-name]
  (fn [instance]
    (if (nil? (attr-name instance))
      (struct +error+ attr-name :presence))))

(def +post-with-validations+
  (compiled-model (assoc +post-map+ :validations [[:title presence]])))

(def valid-instance
  (init +post-with-validations+ {:title "has one"}))

(def invalid-instance
  (init +post-with-validations+ {}))

(deftest "error"
  (assert= (struct +error+ 'on 'cause) (error 'on 'cause))
  (assert= (struct +error+ 'on 'cause 'expected) (error 'on 'cause 'expected)))

(deftest "errors: not on an valid instance before and after validation"
  (assert-not (errors valid-instance))
  (assert-not (errors (validated    valid-instance))))

(deftest "errors: not on invalid instance before validation"
  (assert-not (errors invalid-instance)))

(deftest "errors, validated: returns the errors on a validated invalid instance"
  (let [errs (errors (validated invalid-instance))]
    nil))

(deftest "errors?, valid?, validated: when no invalidities."
  (assert-not (errors? valid-instance))
  (assert-that (valid? valid-instance))
  (assert-not (errors? (validated valid-instance)))
  (assert-that (valid? (validated valid-instance))))

(deftest "errors?, validated: is logically true if an instance has errors"
  (assert-not   (errors? invalid-instance))
  (assert-that (valid? invalid-instance))
  (assert-that (errors? (validated invalid-instance)))
  (assert-not   (valid? (validated invalid-instance))))

