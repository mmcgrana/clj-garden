(ns stash.validators
  (:use stash.core stash.utils))

(def- +url-re+ #"(?i)(^(http|https):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(([0-9]{1,5})?\/.*)?$)")

(defn valid-url
  "Returns a validator that returns a :valid-url error if an instance's value for
  attr-name is not a valid url."
  [attr-name]
  (let [valid-url-error (error attr-name :valid-url)]
    (fn [instance]
      (let [val (get instance attr-name)]
        (if (not (and val (re-match? +url-re+ val)))
          valid-url-error)))))

(defn presence
  "Returns a validator that returns a :presence error if an instance's for
  for attr-name is nil."
  [attr-name]
  (let [presence-error (error attr-name :presence)]
    (fn [instance]
      (let [val (get instance attr-name)]
        (if (nil? val)
          presence-error)))))
