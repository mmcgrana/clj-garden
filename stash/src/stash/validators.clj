(ns stash.validators
  (:use stash.core stash.utils))

(def- +url-re+ #"(?i)(^(http|https):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(([0-9]{1,5})?\/.*)?$)")

(defn valid-url
  "Returns a validator that returns a :valid-url error if an instances value for
  attr-name is not a valid url."
  [attr-name]
  (let [error (struct +error+ attr-name :valid-url)]
    (fn [instance]
      (let [val (get instance attr-name)]
        (if (not (and val (re-matches? +url-re+ val)))
          error)))))
