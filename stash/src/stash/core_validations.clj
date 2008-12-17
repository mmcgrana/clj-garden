(in-ns 'stash.core)

(defstruct +error+ :on :cause :expected)

(defn errors
  "Returns a seq of errors for the given instance. Will be an empty coll if
  there are no errors."
  [instance]
  (get (meta instance) :errors []))

(defn with-error
  "Returns an instance with the error in the errors metadata."
  [instance error]
  (with-meta instance
    (assoc (meta instance) :errors (conj (errors instance) error))))

(defn validated
  "Validates the given instance, returning a new instance that has assocatied
  errors metadata."
  [instance]
  (let [validations (validations (instance-model instance))]
    (reduce
      (fn [instance validator]
        (if-let [error (validator instance)]
          (with-error instance error)
          instance))
      instance
      validations)))

(defn errors?
  "Returns true iff the instance has a non-zero number of errors. Does not
  run any validations itself."
  [instance]
  (not (empty (errors instance))))
