(ns stash.validations)

(defstruct +error+ :on :cause :expected)

(defn errors
  "Returns a seq of errors for the given instance. Will be an empty coll if
  there are no errors."
  [instance]
  (get (meta instance) :errors []))

(defn with-error
  "Returns an instance with the error in the errors metadata."
  [instance error]
  (with-meta instance (assoc (errors instance) error)))

(defn validated
  "Validates the given instance, returning a new instance that has assocatied
  errors metadata."
  [instance]
  (let [validations (:validations (:model (meta model-meta)))]
    (reduce
      (fn [instance validator]
        (if-let [error (validation instance)]
          (with-error instance error)
          instance))
      instance
      validations)))

(defn errors?
  "Returns true iff the instance has a non-zero number of errors. Does not
  run any validations itself."
  [instance]
  (not (empty (errors instance))))


;; Sample validation fns

(defn presence
  "Returns a presence validator for accessor based on options"
  [attr-name]
  (let [error (struct +error+ attr-name :presence)]
    (fn [instance]
      (if (blank? (attr-name instance))
        error))))

(defn min-length
  "Returns a minimum length validator for accessor based on options"
  [attr-name length]
  (let [error (struct +error+ attr-name :min-length length)]
    (fn [instance]
      (let [val (attr-name instance)]
        (if (or (blank? val) (< (.length val) length))
          error)))))
