(ns clj-jdbc.core
  (:use clojure.contrib.except clj-jdbc.utils)
  (:import (java.sql Connection Statement ResultSet)))

(def *connection* nil)
(def *level*      nil)
(def *logger*     nil)

(defmacro with-connection
  "Evaluates body in the context of a new connection to a database from the
  given datasource, then closes the connection when done. If this thread
  has an existing connection to the datasource, that one will be provided
  instead of opening a new one."
  [data-source-form & body]
  `(if *connection*
     ~@body
     (binding [*connection* (.getConnection #^DataSource ~data-source-form)
               *level*      0]
       ~@body)))

(defmacro with-logger
  "Evaluates logger in the context of a logger configured for the thread's
  db actions."
  [logger & body]
  `(binding [*logger* logger]
     ~@body))

(defmacro in-transaction
  "Evaluates body as in transaction on the connection. Updates
  are committed if the execution completes without error or rolled back 
  after an uncaught exception. Supports nested transactions."
  [& body]
  `(do
    (let [level#      *level*
          connection# *connection*]
      (binding [*level* (inc level#)]
        (when (zero? level#)
          (.setAutoCommit connection# false))
        (try
          (returning (do ~@body)
            (when (zero? level#)
              (.commit connection#)))
          (catch Exception e#
            (.rollback connection#)
            (throwf "Transaction rolled back: %s", (.getMessage e#))))))))

(defmacro with-statement
  "Evaluates body in the context of a new Statement for the given conn."
  [binding-sym & body]
  `(let [#^Connection conn# *connection*]
     (with-open [#^Statement ~binding-sym (.createStatement conn#)]
       ~@body)))

(defn log [level sql time]
  "TODOC."
  (if-let [logger *logger*]
    (logger :info [:query {:sql sql :time time}])))

(defmacro with-resultset
  "Evaluates the body in the context of a resultset from the given connection
  based on the given sql."
  [[binding-sym sql-form] & body]
  `(with-statement [#^Statement statement# *connection*]
     (let [sql# ~sql-form
           ~binding-sym
             (try
               (let [[result time] (timed (.executeQuery statement# sql#))]
                 (log :info sql time)
                 result)
               (catch Exception e#
                 (throwf "%s: %s" (.getMessage e#) sql#)))]
       ~@body)))

(defn modify
  "Execute against the given connection the given insert, update, delete, or ddl 
  statement, returning the number of items affected."
  [sql]
  (with-statement [statement *connection*]
    (try
      (let [[result time] (timed (.executeUpdate statement sql))]
        (log :info sql time)
        result)
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