(ns stash.validators)

(defn presence
  "Returns a presence validator for attr-name."
  [attr-name]
  (let [error (struct +error+ attr-name :presence)]
    (fn [instance]
      (if (nil? (attr-name instance))
        error))))

(defn min-length
  "Returns a minimum length validator for attr-name requiring at least a
  specified length."
  [attr-name length]
  (let [error (struct +error+ attr-name :min-length length)]
    (fn [instance]
      (let [val (attr-name instance)]
        (if (or (nil? val) (< (.length val) length))
          error)))))
