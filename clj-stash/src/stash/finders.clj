(ns stash.finders)

(defn find-one-by-sql
  [model sql]
  (if-let [uncast-attrs (jdbc/with-connection [conn (:data-source model)]
                          (jdbc/select-hash conn sql))]
    (crud/instantiate model uncast-attrs)))

(defn find-value-by-sql
  [model sql]
  (jdbc/with-connection [conn (:data-source model)]
    (jdbc/select-value sql)))

(defn stringify-select
  [select])

(defvar- +where-conjunction-strings+
  {:and "AND", :or "OR"})

(defvar- +where-operator-strings+
  {:= "=", :> ">", :>= ">=", :< "<", :<= "<=", :not= "<>"})

(defn compile-where-conjunction
  [conjunction]
  (or (+where-conjunction-strings+ conjunction)
      (throwf "invalid conjunction: %s" conjunction)))

(defn compile-where-operator
  [operator]
  (or (+where-operator-strings+ operator)
      (throwf "invalid operator: %s" operator)))

(defn compile-where-exp
  [where-exp]
  (cond
    (= :not (exp 0))
      (str "(NOT " (compile-where-exp (exp 1)) ")")
    (coll? (exp 1))
      (str "(" (str-join (str  " " (compile-where-conjunction (exp 0)) " ")
                         (map compile-where-exp (rest exp)) ")"))
    (= :in (exp 1)
      (str "(" (the-str (exp 0))
               " IN (" (str-join ", " (map sql-quote (exp 2))) ")") ")")
    :else
      (str "(" (the-str (exp 0)) " "
               (compile-where-operator (exp 1)) " "
               (!quoe-with-slots!)")")

(defn stringify-where
  [where-exp]
  (if where-exp (str " WHERE " (compile-where-exp where-exp))))

(defn stringify-order
  [order]
  (if order
    (str " ORDER BY " (the-str (order 0)) " "
                      (.toUpperCase (the-str (order 1))))))

(defn stringify-limit
  [limit]
  (if limit (str " LIMIT " limit)))

(defn stringify-offset
  [limit]
  (if offset (str " OFFSET " offset)))

(defn- stringify-options
  (str (stringify-where  (:where options))
       (stringify-order  (:order options))
       (stringify-limit  (:limit options))
       (stringify-offset (:offset options))))

(defn options-to-find-sql
  [model options]
  (str "SELECT " (or (stringify-select (:select options)) "*")
       " FROM " (:table-name-str model)
       (stringify-options options)))

(defn find-one
  [model options]
  (find-one-by-sql model
    (options-to-find-sql model
      (merge options {:limit 1}))))

(defn find-all
  [model options]
  (find-all-by-sql model
    (options-to-find-sql model options)))

(defn exists?
  [model options]
  (find-value-by-sql model
    (options-to-find-sql model
      (merge options {:limit 1 :select [:id]}))))

(defn count
  [model options]
  (find-value-by-sql model
    (options-to-find-sql model
      (merge options {:select "count(id)"}))))

(defn minimum
  [model column options])

(defn maximum
  [model column options])

(defn delete-all
  [model options])

(defn find-one-by-sql
  [model sql sets])

(defn find-all-by-sql
  [model sql sets])