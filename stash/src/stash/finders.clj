(ns stash.finders
  (:use stash.utils clojure.contrib.fcase)
  (:require [clj-jdbc.core :as jdbc]
            [stash.crud    :as crud]))

(defn- sql-quote
  [val]
  "Returns a string appropriate for substititution into an sql query to
  literally represent the given val."
  (instance-case val
    String val
    Number (str val)))

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
  [where-exp]
  (cond
    ; (NOT (...))
    (= :not (exp 0))
      (str "(NOT " (where-exp-sql (exp 1)) ")")
    ; (... xxx ... xxx ....)
    (coll? (exp 1))
      (str "(" (str-join (str  " " (where-conjuntion-sql (exp 0)) " ")
                         (map where-exp-sql (rest exp)) ")"))
    ; (.. IN (., ., ., .))
    (= :in (exp 1)
      (str "(" (the-str (exp 0))
               " IN (" (str-join ", " (map sql-quote (exp 2))) ")") ")")
    ; (.. oo ..)
    :else
      (str "(" (the-str (exp 0)) " "
               (where-operator-sql (exp 1)) " "
               (sql-quote (exp 2))")")

(defn- where-sql
  [where-exp]
  (if where-exp (str " WHERE " (where-exp-sql where-exp))))

(defn- order-sql
  [order]
  (if order
    (str " ORDER BY " (the-str (order 0)) " "
                      (upcase (the-str (order 1))))))

(defn- limit-sql
  [limit]
  (if limit (str " LIMIT " limit)))

(defn- offset-sql
  [limit]
  (if offset (str " OFFSET " offset)))

(defn- options-sql
  [options]
  (str (where-sql  (:where options))
       (order-sql  (:order options))
       (limit-sql  (:limit options))
       (offset-sql (:offset options))))

(defn- select-sql
  [selects]
  (if selects
    (if (coll? selects)
      (str-join ", " (map the-str selects))
      (the-str selects))))

(defn- find-sql
  [model options]
  (str "SELECT " (or (select-sql (:select options)) "*")
       " FROM " (:table-name-str model)
       (options-sql options)))

(defn- delete-sql
  [model options]
  (str "DELETE FROM " (:table-name-str model) (options-sql options)))

(defn find-value-by-sql
  "Returns a single value according to the given sql."
  [model sql]
  (jdbc/with-connection [conn (:data-source model)]
    (jdbc/select-value sql)))

(defn find-one-by-sql
  "Returns an instance of model found by the given sql, or nil if no such
  instances are found. "
  [model sql]
  (if-let [uncast-attrs (jdbc/with-connection [conn (:data-source model)]
                          (jdbc/select-hash conn sql))]
    (crud/instantiate model uncast-attrs)))

(defn find-all-by-sql
  "Returns all instances of model found by the given sql."
  [model sql]
  (let [hashes (jdbc/with-connection [conn (:data-source model)]
                 (jdbc/select-hashes conn sql))]
    (map crud/instantiate hashes)))

(defn find-one
  "Returns all instances of model found according to the options."
  [model options]
  (find-one-by-sql model
    (find-sql model (merge options {:limit 1}))))

(defn find-all
  "Returns all instances of model found according to the options."
  [model options]
  (find-all-by-sql model
    (find-sql model options)))

(defn delete-all-by-sql
  "Deletes model's records from the database according to the sql,
  returning the number that were deleted."
  [model sql]
  (jdbc/with-connection [conn (:data-source model)]
    (jdbc/modify conn sql)))

(defn exists?
  "Returns true iff a record for the model exists that corresponds to the
  options."
  [model options]
  (find-value-by-sql model
    (find-sql model (merge options {:limit 1 :select [:id]}))))

(defn count
  "Returns the count of records for the model that correspond to the options."
  [model options]
  (find-value-by-sql model
    (find-sql model (merge options {:select "count(id)"}))))

(defn- extremum
  [model column order options]
  (find-value-by-sql model
    (options-sql model
      (merge options {:select column :order [column order] :limit 1}))))

(defn minimum
  "Returns the minimum value of the column among records the model that
  correspond to the options"
  [model column options]
  (extremum model column :asc options))

(defn maximum
  "Returns the minimum value of the column among records the model that
  correspond to the options"
  [model column options]
  (extremum model column :desc options))

(defn delete-all
  "Deletes all records for the model corresponding to the options, returning
  the number of such records deleted."
  [model options]
  (delete-all-by-sql (options-to-delete-sql model options)))
