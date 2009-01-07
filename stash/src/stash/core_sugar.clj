(in-ns 'stash.core)

(defn inc-attr [instance attr-name]
  "Returns a model with the value for attr-name incremented by 1."
  (update instance attr-name inc))

;(def get-one
;  "Returns an instance corresponding to the record for the given pk(s)."
;  [model pks]
;  (find-one model {:where (pk-where-exp model pks)}))

(defn reload
  "Returns an instance corresponding to the given one but reloaded fresh from
  the db."
  [instance]
  (find-one (instance-model instance) {:where (pk-where-exp instance)}))
