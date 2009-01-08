(ns clj-jdbc.core-test
  (:use clj-unit.core
        clj-jdbc.core
        clojure.contrib.except))

(def test-data-source
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "clj_jdbc_test")
    (.setUser         "mmcgrana")
    (.setPassword     "")))


(defmacro with-test-connection
  [& body]
  `(with-connection ~'test-data-source
     (modify "DELETE FROM fruits")
     (modify "INSERT INTO fruits (id, name) VALUES (1, 'apple'), (2, 'pear'), (3, 'grape')")
     ~@body))

(defmacro defconntest [doc & body]
  "Define a unit test that uses a connection."
  `(deftest ~doc
     (with-test-connection
       ~@body)))

(defconntest "with-connection: reuses connections in nested calls"
  (let [outer-connection (:connection *db*)]
    (with-connection test-data-source
      (assert= outer-connection (:connection *db*)))))

(defconntest "modify: preforms a change and returns affected row count"
  (let [change-count (modify "DELETE FROM fruits WHERE id = 2")
        row-count    (select-value "SELECT count(id) FROM fruits")]
    (list
      (assert= 1 change-count)
      (assert= 2 row-count))))

(defconntest "modify: re-raises exceptions with sql in message"
  (assert-throws #"ERROR.*foobar"
    (modify "foobar")))

(defconntest "modify: reports to reporter if present"
  (with-reporter #(do
                     (assert= "DELETE FROM fruits" %1)
                     (assert-instance Number %2))
    (modify "DELETE FROM fruits")))

(defconntest "with-resultset: re-raises exceptions with sql in message"
  (assert-throws #"ERROR.*foobar"
    (with-resultset [rs "foobar"])))

(defconntest "with-resultset: reports to reporter if present"
  (with-reporter #(do
                    (assert= "SELECT NAME FROM fruits" %1)
                    (assert-instance Number %2))
    (select-value "SELECT NAME FROM fruits")))

(defconntest "select-value returns a single value when row found"
  (assert= "apple"
    (select-value "SELECT name FROM fruits WHERE id = 1")))

(defconntest "select-value returns nil when no rows found"
  (assert-nil
    (select-value "SELECT name FROM fruits WHERE id = 4")))

(defconntest "select-values returns a seq of tuples when rows found"
  (assert=
    '("apple" "pear" "grape")
    (select-values "SELECT name FROM fruits ORDER BY id")))

(defconntest "select-values returns nil when no rows found"
  (assert-nil (select-values "SELECT name FROM fruits WHERE id > 3")))


(defconntest "select-tuple returns a single tuple when row found"
  (assert= `(1 "apple")
    (select-tuple "SELECT id, name FROM fruits WHERE id = 1")))

(defconntest "select-tuple returns nil when no rows found"
  (assert-nil
    (select-tuple "SELECT id, name FROM fruits WHERE id = 4")))

(defconntest "select-tuples returns a seq of tuples when rows found"
  (assert=
    '((1 "apple") (2 "pear") (3 "grape"))
    (select-tuples "SELECT id, name FROM fruits ORDER BY id")))

(defconntest "select-tuples returns nil when no rows found"
  (assert-nil (select-tuples "SELECT id, name FROM fruits WHERE id > 3")))


(defconntest "select-map returns a single map when row found"
  (assert= {:id 1 :name "apple"}
    (select-map "SELECT id, name FROM fruits WHERE id = 1")))

(defconntest "select-maps returns nil when no rows found"
  (assert-nil
    (select-map "SELECT id, name FROM fruits WHERE id = 4")))

(defconntest "select-maps returns a seq of maps when rows found"
  (assert=
    '({:id 1 :name "apple"} {:id 2 :name "pear"} {:id 3 :name "grape"})
    (select-maps "SELECT id, name FROM fruits ORDER BY id")))

(defconntest "select-maps returns nil when no rows found"
  (assert-nil (select-tuples "SELECT id, name FROM fruits WHERE id > 3")))

(defconntest "in-transaction: returns the value on a success"
  (in-transaction
    (assert= "apple"
      (select-value "SELECT name FROM fruits where id = 1"))))

(defconntest "in-transaction: propogates exceptions"
  (assert-throws #"o noes"
    (in-transaction
      (throwf "o noes"))))

(defconntest "in-transaction: rolls back changes after exceptions"
  (try
    (in-transaction
      (modify "INSERT INTO fruits (id, name) VALUES (4,'orange')")
      (throwf "o noes"))
    (catch Exception e))
  (assert= 3 (select-value "SELECT count(id) FROM fruits")))

(defconntest "in-transaction: allows nesting without autocommit"
  (try
    (in-transaction
      (modify "INSERT INTO fruits (id, name) VALUES (4,'orange')")
      (in-transaction
        (modify "INSERT INTO fruits (id, name) VALUES (5,'bannana')"))
      (throwf "o noes"))
    (catch Exception e))
  (assert= 3 (select-value "SELECT count(id) FROM fruits")))
