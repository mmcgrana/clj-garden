(defn validate
  [instance]
  (let [validations (:validations (:model (meta instance)))]
    ))

(defn errors
  [instance]
  (:errors (meta instance)))

(defn errors?
  [instance]
  (not (empty (errors instance))))

