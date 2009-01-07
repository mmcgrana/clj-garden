(in-ns 'stash.migrations)

(defn- primary-keys-sql
  [column-names]
  (str "PRIMARY KEY (" (str-join "," (map name column-names)) ")"))

(defn- column-sql
  [[name-key type opts]]
  (str (name name-key) " " (stash/type-db-type type)
    (if (get opts :unique) " UNIQUE")
    (if-not (get opts :nullable) " NOT NULL")))

(defn- columns-sql
  [column-defs primary-keys]
  (str "("
    (str-join ", " (map column-sql column-defs))
    ", " (primary-keys-sql primary-keys) ")"))

(defmacro- defddl [ddl-name ddl-doc ddl-sql-args ddl-sql-body]
  (let [ddl-sql-name (symbol (str ddl-name "-sql"))
        ddl-sql-doc  (str "Returns sql needed for " ddl-name)]
   `(do
      (defn ~ddl-sql-name
        ~ddl-sql-doc
        ~ddl-sql-args
        ~ddl-sql-body)
      (defn ~ddl-name
        ~ddl-doc
        ~(vec (cons 'conn ddl-sql-args))
        (jdbc/modify ~'conn ~(cons ddl-sql-name ddl-sql-args))))))

(defddl create-table
  "Create a table named table-name with column names and types as specified by
  column-defs."
  [table-name column-defs]
  (let [pks ((ns-resolve 'stash.core 'compiled-pk-column-names) column-defs)]
    (str "CREATE TABLE " (name table-name) " " (columns-sql column-defs pks))))

(defddl rename-table
  "Rename a table."
  [from-table-name to-table-name]
  (str "ALTER TABLE " (name from-table-name)
       " RENAME TO " (name to-table-name)))

(defddl drop-table
  "Drop a table."
  [table-name]
  (str "DROP TABLE " (name table-name)))

(defddl add-column
  "Add a column to a table according to a column-def vector."
  [table-name column-def]
  (str "ALTER TABLE " (name table-name) " ADD COLUMN " (column-sql column-def)))

(defddl rename-column
  "Rename a single column in a table."
  [table-name from-column-name to-column-name]
  (str "ALTER TABLE " (name table-name) " RENAME COLUMN "
       (name from-column-name) " TO " (name to-column-name)))

(defddl drop-column
  "Drop a single column from a table."
  [table-name column-name]
  (str "ALTER TABLE " (name table-name) " DROP COLUMN " (name column-name)))

(defn- index-name
  [table-name column-names]
  (str (name table-name) "_by_" (str-join "_and_" (map name column-names))))

(defddl create-index
  "Add a single index for one or more columns in a table."
  [table-name column-names]
  (str "CREATE INDEX " (index-name table-name column-names) " ON "
       (name table-name) " (" (str-join ", " (map name column-names)) ")"))

(defddl drop-index
  "Drop a single index for one or more columns in a table."
  [table-name column-names]
  (str "DROP INDEX " (index-name table-name column-names)))
