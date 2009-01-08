(ns clj-jdbc.core
  (:use clojure.contrib.except clj-jdbc.utils)
  (:import (java.sql Connection Statement ResultSet)
           (javax.sql DataSource)))

(def *db*       nil)
(def *db-reporter* nil)

(defn report-db [sql & [time]]
  "Report the sql query run and optionally the time it took to the bound
  query reporter."
  (if-let [reporter *db-reporter*]
    (reporter sql time)))

(defmacro with-reporter
  "Execute body in the context of a dynamically bound query reporter.
  The reporter is an fn of two args - the sql executed and the query time
  in milliseconds."
  [reporter-form & body]
  `(binding [*db-reporter* ~reporter-form]
     ~@body))

(defmacro with-connection
  "Evaluates body in the context of a new connection to a database from the
  given datasource, then closes the connection when done. If this thread
  has an existing connection to the datasource, that one will be provided
  instead of opening a new one."
  [data-source-form & body]
  `(if *db*
     (do ~@body)
     (with-open [connection# (.getConnection #^DataSource ~data-source-form)]
       (binding [*db* {:connection connection# :level 0}]
         ~@body))))

(defmacro in-transaction
  "Evaluates body as in transaction on the connection. Updates
  are committed if the execution completes without error or rolled back 
  after an uncaught exception. Supports nested transactions."
  [& body]
  `(do
    (let [db#         *db*
          level#      (:level db#)
          connection# (:connection db#)]
      (binding [*db* (assoc db# :level (inc (get db# :level)))]
        (when (zero? level#)
          (with-realtime [n# (.setAutoCommit connection# false)]
            (report-db "BEGIN" n#)))
        (try
          (returning (do ~@body)
            (when (zero? level#)
              (with-realtime [n# (.commit connection#)]
                (report-db "COMMIT" n#))))
          (catch Exception e#
            (let [n# (realtime (.rollback connection#))]
              (report-db "ROLLBACK" n#))
            (throwf "Transaction rolled back: %s", (.getMessage e#))))))))

(defmacro with-statement
  "Evaluates body in the context of a new Statement for the given conn."
  [[binding-sym connection-sym] & body]
  `(with-open [~binding-sym (.createStatement ~connection-sym)]
     ~@body))

(defmacro with-resultset
  "Evaluates the body in the context of a resultset from the given connection
  based on the given sql."
  [[binding-sym sql-form] & body]
  `(with-statement [#^Statement statement# (:connection *db*)]
     (let [sql# ~sql-form
           ~binding-sym
             (try
               (with-realtime [n# (.executeQuery statement# sql#)]
                 (report-db sql# n#))
               (catch Exception e#
                 (throwf "%s: %s" (.getMessage e#) sql#)))]
       ~@body)))

(defn modify
  "Execute against the given connection the given insert, update, delete, or ddl 
  statement, returning the number of items affected."
  [sql]
  (with-statement [statement (:connection *db*)]
    (try
      (with-realtime [n (.executeUpdate statement sql)]
        (report-db sql n))
      (catch Exception e
        (throwf "%s: %s" (.getMessage e) sql)))))

(defn resultset-values
  "Returns a lazy seq of single values corresponding to the resultset's rows."
  [#^ResultSet rs]
  (when (.next rs)
    (lazy-cons (.getObject rs 1) (resultset-values rs))))

(defn resultset-tuples
  "Returns a lazy seq of value tuples corresponding to the resultset's rows."
  [#^ResultSet rs]
  (let [idxs   (range 1 (inc (.getColumnCount (.getMetaData rs))))
        tuples (fn thisfn []
                 (when (.next rs)
                   (lazy-cons
                     (doall (map (fn [#^Integer i] (.getObject rs i)) idxs))
                     (thisfn))))]
    (tuples)))

(defn resultset-maps
  "Returns a lazy seq of maps corresponding to the column names and values in
  the resultset's rows."
  [#^ResultSet rs]
  (let [rsmeta (.getMetaData rs)
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
  "Returns a fully realized seq of values corresponding to the first columns of 
  the rows of the  result of the given sql query, or nil if no results were 
  found."
  [sql]
  (with-resultset [rs sql]
    (doall (resultset-values rs))))

(defn select-value
  "Returns a single valeu for the first column of the first result of the given 
  sql query, or nil if no results were found."
  [sql]
  (with-resultset [rs sql]
    (first (resultset-values rs))))

(defn select-tuples
  "Returns a seq of tuples corresponding to the rows of the result of the given 
  sql query, or an empty seq if no results were found."
  [sql]
  (with-resultset [rs sql]
    (doall (resultset-tuples rs))))

(defn select-tuple
  "Returns a single tuple for the first result of the given sql query, or nil if
  no results were found."
  [sql]
  (with-resultset [rs sql]
    (first (resultset-tuples rs))))

(defn select-maps
  "Returns a seq of maps corresponding to the column names and corresponding 
  values of the rows of the result of the given sql query, or an empty seq if 
  no results were found."
  [sql]
  (with-resultset [rs sql]
    (doall (resultset-maps rs))))

(defn select-map
  "Returns a single map of column names to values for the first result of the 
  given sql query, or nil if no results were found."
  [sql]
  (with-resultset [rs sql]
    (first (resultset-maps rs))))