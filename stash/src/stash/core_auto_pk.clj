(in-ns 'stash.core)

(defn auto-uuid
  "Returns a random new UUID String."
  []
  (str (java.util.UUID/randomUUID)))

(declare execute)

(defn auto-integer
  "Returns the next integer in the auto pk secuence."
  [table-name column-name data-source logger]
  (let [seq-name (str (name table-name) "_" (name column-name) "_seq")
        sql      (str "SELECT nextval('" seq-name "')")]
    (execute jdbc/select-value data-source sql logger)))
