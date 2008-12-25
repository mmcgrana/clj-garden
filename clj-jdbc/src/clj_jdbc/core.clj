(ns clj-jdbc.core
  (:use clojure.contrib.except)
  (:import (java.sql Connection Statement ResultSet)))

(def *connections* {})

(defn require-driver
  "Ensure that the driver corresponding to the given class name is loaded."
  [classname]
  (Class/forName classname))

(defmacro with-connection
  "Evaluates body in the context of a new connection to a database from the
  given datasource, then closes the connection when done. If this thread
  has an existing connection to the datasource, that one will be provided
  instead of opening a new one."
  [[binding-sym #^DataSource data-source-form] & body]
  `(let [data-source# ~data-source-form]
     (if-let [existing-conn# (get *connections* data-source#)]
       (let [~binding-sym existing-conn#]
         ~@body)
       (with-open [new-conn# (.getConnection data-source#)]
         (binding [*connections* (assoc *connections* data-source# new-conn#)]
           (let [~binding-sym new-conn#]
              ~@body))))))

(defmacro with-statement
  "Evaluates body in the context of a new Statement for the given conn."
  [[binding-sym #^Connection conn-sym] & body]
  `(with-open [~binding-sym (.createStatement ~conn-sym)]
     ~@body))

(defmacro with-resultset
  "Evaluates the body in the context of a resultset from the given connection
  based on the given sql."
  [[binding-sym #^Connection conn-sym sql-form] & body]
  `(with-statement [statement# ~conn-sym]
     (let [sql# ~sql-form
           ~binding-sym
             (try
               (.executeQuery statement# sql#)
               (catch Exception e#
                 (throwf "%s: %s" (.getMessage e#) sql#)))]
       ~@body)))

(defn modify
  "Execute against the given connection the given insert, update, delete, or ddl 
  statement, returning the number of items affected."
  [#^Connection conn sql]
  (with-statement [statement conn]
    (try
      (.executeUpdate statement sql)
      (catch Exception e
        (throwf "%s: %s" (.getMessage e) sql)))))

(defn resultset-values
  "Returns a lazy seq of single values corresponding to the resultset's rows."
  [#^ResultSet rs]
  (when (.next rs)
    (lazy-cons (.getObject rs 1) (resultset-values rs))))

(defn resultset-tuples
  "Returns a lazy seq of value tuples corresponding to the resultset's rows."
  [#^java.sql.ResultSet rs]
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
  [#^java.sql.ResultSet rs]
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
  [conn sql]
  (with-resultset [rs conn sql]
    (doall (resultset-values rs))))

(defn select-value
  "Returns a single valeu for the first column of the first result of the given 
  sql query, or nil if no results were found."
  [conn sql]
  (with-resultset [rs conn sql]
    (first (resultset-values rs))))

(defn select-tuples
  "Returns a seq of tuples corresponding to the rows of the result of the given 
  sql query, or an empty seq if no results were found."
  [conn sql]
  (with-resultset [rs conn sql]
    (doall (resultset-tuples rs))))

(defn select-tuple
  "Returns a single tuple for the first result of the given sql query, or nil if
  no results were found."
  [conn sql]
  (with-resultset [rs conn sql]
    (first (resultset-tuples rs))))

(defn select-maps
  "Returns a seq of maps corresponding to the column names and corresponding 
  values of the rows of the result of the given sql query, or an empty seq if 
  no results were found."
  [conn sql]
  (with-resultset [rs conn sql]
    (doall (resultset-maps rs))))

(defn select-map
  "Returns a single map of column names to values for the first result of the 
  given sql query, or nil if no results were found."
  [conn sql]
  (with-resultset [rs conn sql]
    (first (resultset-maps rs))))

(defmacro in-transaction
  "Evaluates body as a transaction on the given database connection. Updates
  are committed together or rolled back after an uncaught exception. 
  Supports nested transactions."
  [conn-sym & body]
  `(do
     (.setAutoCommit ~conn-sym false)
     (try
       (let [ret# (do ~@body)]
         (.commit ~conn-sym)
         ret#)
       (catch Exception e#
         (.rollback ~conn-sym)
         (throwf "Transaction rolled back: %s", (.getMessage e#))))))