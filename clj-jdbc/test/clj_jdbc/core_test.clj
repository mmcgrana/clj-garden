(ns clj-jdbc.core-test
  (:use clj-unit.core
        clj-jdbc.core
        clojure.contrib.except))

(def test-data-source
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "clj-jdbc-test")
    (.setUser         "mmcgrana")
    (.setPassword     "")))

(defmacro with-test-connection
  [conn-sym & body]
  `(with-connection [~conn-sym ~'test-data-source]
     (modify ~conn-sym "DELETE FROM fruits")
     (modify ~conn-sym "INSERT INTO fruits (id, name) VALUES
                        (1, 'apple'), (2, 'pear'), (3, 'grape')")
     ~@body))

(defmacro defconntest [doc conn-sym & body]
  "Define a unit test that uses a connection."
  `(deftest ~doc
     (with-test-connection ~conn-sym
       ~@body)))

(defconntest "modify preforms a change and returns affected row count" conn
  (let [change-count (modify conn "DELETE FROM fruits WHERE id = 2")
        row-count    (select-value conn "SELECT count(id) FROM fruits")]
    (list
      (assert= 1 change-count)
      (assert= 2 row-count))))


(defconntest "select-value returns a single value when row found" conn
  (assert= "apple"
    (select-value conn "SELECT name FROM fruits WHERE id = 1")))

(defconntest "select-value returns nil when no rows found" conn
  (assert-nil
    (select-value conn "SELECT name FROM fruits WHERE id = 4")))

(defconntest "select-values returns a seq of tuples when rows found" conn
  (assert=
    '("apple" "pear" "grape")
    (select-values conn "SELECT name FROM fruits ORDER BY id")))

(defconntest "select-values returns nil when no rows found" conn
  (assert-nil (select-values conn "SELECT name FROM fruits WHERE id > 3")))


(defconntest "select-tuple returns a single tuple when row found" conn
  (assert= `(1 "apple")
    (select-tuple conn "SELECT id, name FROM fruits WHERE id = 1")))

(defconntest "select-tuple returns nil when no rows found" conn
  (assert-nil
    (select-tuple conn "SELECT id, name FROM fruits WHERE id = 4")))

(defconntest "select-tuples returns a seq of tuples when rows found" conn
  (assert=
    '((1 "apple") (2 "pear") (3 "grape"))
    (select-tuples conn "SELECT id, name FROM fruits ORDER BY id")))

(defconntest "select-tuples returns nil when no rows found" conn
  (assert-nil (select-tuples conn "SELECT id, name FROM fruits WHERE id > 3")))


(defconntest "select-map returns a single map when row found" conn
  (assert= {:id 1 :name "apple"}
    (select-map conn "SELECT id, name FROM fruits WHERE id = 1")))

(defconntest "select-maps returns nil when no rows found" conn
  (assert-nil
    (select-map conn "SELECT id, name FROM fruits WHERE id = 4")))

(defconntest "select-maps returns a seq of maps when rows found" conn
  (assert=
    '({:id 1 :name "apple"} {:id 2 :name "pear"} {:id 3 :name "grape"})
    (select-maps conn "SELECT id, name FROM fruits ORDER BY id")))

(defconntest "select-maps returns nil when no rows found" conn
  (assert-nil (select-tuples conn "SELECT id, name FROM fruits WHERE id > 3")))

(defconntest "in-transaction: returns the value on a success" conn
  (in-transaction conn
    (assert= "apple"
      (select-value conn "SELECT name FROM fruits where id = 1"))))

(defconntest "in-transaction propogates exceptions" conn
  (in-transaction conn
    (assert-throws #"Transaction rolled back:.*bogus sql"
      (select-value conn "bogus sql"))))

(defconntest "in-transaction: rolls back changes after exceptions" conn
  (try
    (in-transaction conn
      (modify conn "INSERT INTO fruits (id, name) VALUES (4,'orange')")
      (throwf "o noes"))
    (catch Exception e))
  (assert= 3 (select-value conn "SELECT count(id) FROM fruits")))
