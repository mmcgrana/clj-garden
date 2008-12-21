(in-ns 'stash.core)

(def- +where-conjunction-strings+
  {:and "AND", :or "OR"})

(def- +where-operator-strings+
  {:= "=", :> ">", :>= ">=", :< "<", :<= "<=", :not= "<>"})

(defn- where-conjunction-sql
  [conjunction]
  (or (+where-conjunction-strings+ conjunction)
      (throwf "invalid conjunction: %s" conjunction)))

(defn- where-operator-sql
  [operator]
  (or (+where-operator-strings+ operator)
      (throwf "invalid operator: %s" operator)))

(defn- where-exp-sql
  [model where-exp]
  (cond
    ; [:not <more>]
    (= :not (where-exp 0))
      (str "(NOT " (where-exp-sql model (where-exp 1)) ")")

    ; [:foo :in '(1 2 3)]
    (= :in (where-exp 1))
      (let [c-quoter ((quoters-by-name model) (where-exp 0))]
        (str "(" (name (where-exp 0))
               " IN ("
                 (str-join ", " (map #(c-quoter %) (where-exp 2)))
               ")"
            ")"))

    ; [:and [<more> <more> <more>]]
    (coll? (where-exp 1))
      (let [conj-str (str " " (where-conjunction-sql (where-exp 0)) " ")
            inners   (map (partial where-exp-sql model) (rest where-exp))]
        (str "(" (str-join conj-str inners)")"))

    ; [:foo :> 20]
    :else
      (str "(" (name (where-exp 0)) " "
               (where-operator-sql (where-exp 1)) " "
               (((quoters-by-name model) (where-exp 0)) (where-exp 2)) ")")))

(defn- where-sql
  [model where-exp]
  (if where-exp (str " WHERE " (where-exp-sql model where-exp))))

(defn- order-sql
  [order]
  (if order
    (str " ORDER BY " (name (order 0)) " "
                      (.toUpperCase (name (order 1))))))

(defn- limit-sql
  [limit]
  (if limit (str " LIMIT " limit)))

(defn- offset-sql
  [offset]
  (if offset (str " OFFSET " offset)))

(defn- qualifiers-sql
  [model qualifiers]
  (str (where-sql  model (get qualifiers :where))
       (order-sql  (get qualifiers :order))
       (limit-sql  (get qualifiers :limit))
       (offset-sql (get qualifiers :offset))))

(defn- select-sql
  [selects]
  (if selects
    (if (coll? selects)
      (str-join ", " (map name selects))
      (the-str selects))))

(defn find-sql
  "Returns the sql query string for the model based on the options."
  [model & [options]]
  (str "SELECT " (or (select-sql (get options :select)) "*")
       " FROM " (table-name-str model)
       (qualifiers-sql model options)))

(defn delete-all-sql
  [model & [options]]
  (str "DELETE FROM " (table-name-str model) (qualifiers-sql model options)))

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
  (find-value-by-sql model
    (find-sql model (merge options {:limit 1 :select [:id]}))))

(defn count-all
  "Returns the count of records for the model that correspond to the options."
  [model & [options]]
  (find-value-by-sql model
    (find-sql model (merge options {:select "count(id)"}))))

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
