(in-ns 'stash.core)

(defn find-value-by-sql
  "Returns a single uncast value according to the given sql."
  [model sql]
  (jdbc/with-connection [conn (data-source model)]
    (jdbc/select-value conn sql)))

(defn find-one-by-sql
  "Returns an instance of model found by the given sql, or nil if no such
  instances are found. "
  [model sql]
  (if-let [uncast-attrs (jdbc/with-connection [conn (data-source model)]
                          (jdbc/select-map conn sql))]
    (instantiate model uncast-attrs)))

(defn find-all-by-sql
  "Returns all instances of model found by the given sql."
  [model sql]
  (let [uncast-attrs (jdbc/with-connection [conn (data-source model)]
                       (jdbc/select-maps conn sql))]
    (map (partial instantiate model) uncast-attrs)))

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

(defn delete-all-by-sql
  "Deletes model's records from the database according to the sql,
  returning the number that were deleted."
  [model sql]
  (jdbc/with-connection [conn (data-source model)]
    (jdbc/modify conn sql)))

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
