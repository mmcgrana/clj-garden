(ns clj-jdbc.core
  (:use [clojure.contrib.def :only (defvar- defmacro-)]))

(defn require-driver
  "Ensure that the driver corresponding to the given class name is loaded."
  [classname]
  (Class/forName classname))

(defmacro with-connection
  "Evaluates body in the context of a new connection to a database from the
  given datasource, then closes the connection when done."
  [[binding-sym data-source-form] & body]
  `(with-open [~binding-sym (.getConnection ~data-source-form)]
     ~@body))

(defn- prepare-statement
  "Return a PreparedStatement for the given conn corresponding to the given 
  query and query values."
  [#^java.sql.Connection conn query sets]
  (let [prepared-statement (.prepareStatement conn query)]
    (loop [sets-rest sets i 1]
      (when (seq sets-rest)
        (.setObject prepared-statement i (first sets-rest))
        (recur (rest sets-rest) (inc 1))))
    prepared-statement))

(defn modify
  "Execute against the given connection the given insert, update, delete, or ddl 
  statement, returning the number of items affected."
  [#^java.sql.Connection conn sql & [sets]]
  (with-open [#^java.sql.PreparedStatement prepared-statement
                (prepare-statement conn sql sets)]
    (.executeUpdate prepared-statement)))

(defmacro- with-resultset
  [resultset-sym conn-sym sql-sym sets-sym body-form]
  `(with-open [#^java.sql.PreparedStatement prepared-statement#
                (prepare-statement ~conn-sym ~sql-sym ~sets-sym)]
     (let [~resultset-sym (.executeQuery prepared-statement#)]
       ~body-form)))

(defn- resultset-values
  "Returns a lazy seq of single values corresponding to the resultset's rows."
  [#^java.sql.ResultSet rs]
  (when (.next rs)
    (lazy-cons (.getObject rs 1) (resultset-values rs))))

(defn- resultset-tuples
  "Returns a lazy seq of value tuples corresponding to the resultset's rows."
  [#^java.sql.ResultSet rs]
  (let [idxs   (range 1 (inc (.getColumnCount (.getMetaData rs))))
        tuples (fn thisfn []
                 (when (.next rs)
                   (lazy-cons
                     (doall (map (fn [#^Integer i] (.getObject rs i)) idxs))
                     (thisfn))))]
    (tuples)))

(defn- resultset-maps
  "Returns a lazy seq of maps corresponding to the column names and values in
  the resultset's rows."
  [#^java.sql.ResultSet rs]
  (let [rsmeta (.getMetaDat rs)
        idxs   (range 1 (inc (.getColumnCount rsmeta)))
        keys   (map
                 (fn [i] (keyword (.toLowerCase (.getColumnName rsmeta i))))
                 idxs)
        maps   (fn thisfn []
                 (when (.next rs)
                   (lazy-cons
                     (zipmap
                       keys (map (fn [#^Integer i] (.getObject rs i)) idxs))
                     (thisfn))))]
    (maps)))

(defn select-values
  "Returns a seq of values corresponding to the first columns of the rows of the 
  result of the given sql query, or nil if no results were found."
  [conn sql & [sets]]
  (with-resultset rs conn sql sets
    (doall (resultset-values rs))))

(defn select-value
  "Returns a single valeu for the first column of the first result of the given 
  sql query, or nil if no results were found."
  [conn sql & [sets]]
  (with-resultset rs conn sql sets
    (first (resultset-values rs))))

(defn select-tuples
  "Returns a seq of tuples corresponding to the rows of the result of the given 
  sql query, or an empty seq if no results were found."
  [conn sql & [sets]]
  (with-resultset rs conn sql sets
    (doall (resultset-tuples rs))))

(defn select-tuple
  "Returns a single tuple for the first result of the given sql query, or nil if
  no results were found."
  [conn sql & [sets]]
  (with-resultset rs conn sql sets
    (first (resultset-tuples rs))))

(defn select-maps
  "Returns a seq of maps corresponding to the column names and corresponding 
  values of the rows of the result of the given sql query, or an empty seq if 
  no results were found."
  [conn sql & [sets]]
  (with-resultset rs conn sql sets
    (doall (resultset-maps rs))))

(defn select-map
  "Returns a single map of column names to values for the first result of the 
  given sql query, or nil if no results were found."
  [conn sql & [sets]]
  (with-resultset rs conn sql sets
    (first (resultset-maps rs))))

(defmacro with-transaction
  "Evaluates body as a transaction on the given database connection. Updates
  are committed together or rolled back after an uncaught exception. Does not 
  support nested transactions."
  [conn-sym & body]
  `(do
     (.setAutoCommit ~conn-sym false)
     (try
       (let [ret# (do ~@body)]
         (.commit ~conn-sym)
         ret#)
       (catch Exception e#
         (.rollback ~conn-sym)
         (throw (Exception.
           (format "Transaction rolled back: %s", (.getMessage e#)) e#))))))