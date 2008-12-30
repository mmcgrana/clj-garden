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