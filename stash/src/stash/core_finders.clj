(in-ns 'stash.core)

(defn find-value-by-sql
  "Returns a single uncast value according to the given sql."
  [model sql]
  (execute jdbc/select-value (data-source model) sql (logger model)))

(defn find-one-by-sql
  "Returns an instance of model found by the given sql, or nil if no such
  instances are found. "
  [model sql]
  (if-let [uncast-attrs (execute jdbc/select-map (data-source model)
                          sql (logger model))]
    (instantiate model uncast-attrs)))

(defn find-all-by-sql
  "Returns all instances of model found by the given sql."
  [model sql]
  (let [uncast-attrs (execute jdbc/select-maps (data-source model)
                       sql (logger model))]
    (map #(instantiate model %) uncast-attrs)))

(defn find-one
  "Returns all instances of model found according to the options."
  [model & [options]]
  (find-one-by-sql model
    (find-sql model (merge options {:limit 1}))))

(defn find-all
  "Returns all instances of model found according to the options."
  [model & [options]]
  (find-all-by-sql model
    (find-sql model options)))

(defn get-one
  "Returns an instance corresponding to the record for the given pk val(s)."
  [model pk-val-or-vals]
  (let [pk-vals (if (coll? pk-val-or-vals) pk-val-or-vals [pk-val-or-vals])]
    (find-one model {:where (pk-where-exp (pk-column-names model) pk-vals)})))

(defn reload
  "Returns an instance corresponding to the given one but reloaded fresh from
  the db."
  [instance]
  (find-one (instance-model instance) {:where (pk-where-exp instance)}))

(defn delete-all-by-sql
  "Deletes model's records from the database according to the sql,
  returning the number that were deleted."
  [model sql]
  (execute jdbc/modify (data-source model) sql (logger model)))

(defn exist?
  "Returns true iff a record for the model exists that corresponds to the
  options."
  [model & [options]]
  (find-one model (merge options {:limit 1 :select (pk-column-names model)})))

(defn count-all
  "Returns the count of records for the model that correspond to the options."
  [model & [options]]
  (find-value-by-sql model
    (find-sql model
      (merge options
        {:select "count(*)"}))))

(defn- extremum
  "Helper for minimum and maxium."
  [model attr-name order options]
  (let [parser ((parsers-by-name model) attr-name)]
    (parser
      (find-value-by-sql model
        (find-sql model
          (merge options {:select [attr-name] :order [attr-name order]}))))))

(defn minimum
  "Returns the minimum value of the column among records the model that
  correspond to the options"
  [model attr-name & [options]]
  (extremum model attr-name :asc options))

(defn maximum
  "Returns the minimum value of the column among records the model that
  correspond to the options"
  [model attr-name & [options]]
  (extremum model attr-name :desc options))

(defn delete-all
  "Deletes all records for the model corresponding to the options, returning
  the number of such records deleted."
  [model & [options]]
  (delete-all-by-sql model (delete-all-sql model options)))

(defn destroy-all
  "Destroys all records for the model corresponding to the options, running
  callbacks as appropriate. Returns the number of records deleted."
  [model & [options]]
  (doseq [instance (find-all model options)]
    (destroy instance)))
