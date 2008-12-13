(ns stash.validations)

(defstruct +error+ :on :cause :expected)

; validations as seq of [validator options pairs, options w/ :if and :if-not]
(defn with-error
  [instance error]
  (!))

(defn validate?
  [instance options]
  (!))

(defn validate
  "Validates the given instance, returning a new instance that has assocatied
  errors metadata and that has been transformed by any validation-related
  callbacks."
  [instance]
  (let [validations (:validations (:model (meta instance)))]
    (reduce
      (fn [instance [validator options]]
        (if-let [error (and (validate? instance options)
                            (validation instance))]
          (with-error instance error)
          instance))
      instance
      validations)))

(defn errors
  "Returns a seq of errors for the given instance. Will be an empty seq if
  there are no errors."
  [instance]
  (:errors (meta instance) []))

(defn errors?
  "Returns true iff the instance has a non-zero number of errors. Does not
  run any validations itself."
  [instance]
  (not (empty (errors instance))))

(defn presence
  "Returns a presence validator validator for accessor based on options"
  [accessor options]
  (let [error (struct +error+ attr-name :presence)]
    (fn [instance]
      (if (blank? (accessor instance))
        error))))

(defn min-length
  "Returns a minimum length validator for accessor based on options"
  [accessor options]
  (let [min   (:min options)
        error (struct +error+ accessor)]))

