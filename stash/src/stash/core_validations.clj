(in-ns 'stash.core)

(defstruct +error+ :on :cause :expected)

(defn error
  "Constructor for +error+ structs."
  [on cause & [expected]]
  (struct +error+ on cause expected))

(defn errors
  "Returns a coll of errors for the given instance, or nil if there are none."
  [instance]
  (get (meta instance) :errors))

(defn with-error
  "Returns an instance with the error in the errors metadata."
  [instance error]
  (update-meta-by instance :errors #(conj (or % []) error)))

(defn validated
  "Validates the given instance, returning a new instance that has assocatied
  errors metadata."
  [instance]
  (let [validators (validators (instance-model instance))]
    (reduce
      (fn [instance validator]
        (if-let [error (validator instance)]
          (with-error instance error)
          instance))
      instance
      validators)))

(defn valid?
  "Returns true iff the instance has no errors. Does not run any validations
  itself."
  [instance]
  (empty? (errors instance)))

(defn errors?
  "Returns true iff the instance has a non-zero number of errors. Does not
  run any validations itself."
  [instance]
  (not (valid? instance)))
