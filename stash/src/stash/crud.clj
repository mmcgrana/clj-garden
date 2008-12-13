(ns stash.crud
  (:require [clj-jdbc.core :as jdbc])
  (:use stash.utils))

; model
;   : data-source
;   : table-name(-str)
;   : columns
; 
; column
;   ; str-name
;   ; name

(defn insert-sql
  "Returns the insert sql for the instance."
  [instance]
  (let [model   (:model (meta instance))
        columns (:columns model)]
    (str "INSERT INTO " (:table-name-str model) " "
         "(" (str-join ", " (map :name-str columns)) ") "
         "VALUES "
         "(" (str-join ", "
               (map (fn [column] (sql-quote ((:name column) instance)))
                    column-names) ")")))

(defn update-sql
  "Returns the update sql for the instance."
  [instance]
  (let [model (:model (meta instance))]
    (str "UPDATE " (:table-name-str model) " SET "
         (str-join ","
           (map (fn [column]
                  (str (:name-str column) " = "
                       (sql-quote ((:name column) instance))))
                (:columns model))) " "
         "WHERE id = '" (:id instance) "'")))

(defn delete-sql
  "Returns the delete sql for the instance."
  [instance]
  (let [model (:model (meta instance))]
    (str "DELETE FROM " (:table-name-str model) " "
         "WHERE id = '" (:id instance) "'")))

(defn persist-insert
  "Persists the new instance to the database, returns an instance
  that is no longer marked as new."
  [instance]
  (let [sql (insert-sql instance)]
    (jdbc/with-connection [conn (:data-source (:model (meta instance)))]
      (jdbc/modify conn sql))
    (with-updated-meta instance {:new false})))

(defn persist-update
  "Persists all of the instance to the database.
  TODO: return value."
  [instance]
  (let [sql (update-sql instance)]
    (jdbc/with-connection [conn (:data-source (:model (meta instance)))]
      (jdbc/modify conn sql)))))

(defn persist
  "Persist the new or non-new instance to the database.
  TODO: return value."
  [instance]
  ((if (new? instance) persist-insert persis-update) instance))

(defn delete
  "Delete the instance from the database. Returns an instance indicating that
  it has been deleted."
  [instance]
  (let [sql (delete-sql instance)]
    (jdbc/with-connection [conn (data-source instance)]
      (jdbc/modify conn sql))
    (with-updated-meta instance {:deleted true})))

(defn new
  "Returns an instance of the model with the given attrs having new status."
  [model attrs]
  (with-meta {:model model :new true} (assoc attrs :id (uuid/gen))))

(defn instantiate
  "Returns an instance of the model with the given attrs having non-new
  status. "
  [model attrs]
  (with-meta {:model model} attrs))

(defn create
  "Creates and returns an instance of the model with the given attrs."
  [model attrs]
  (save (new model attrs)))

(defn new?
  "Returns true if the instance has not been saved to the database."
  [instance]
  (:new (meta instance)))

(defn save
  "Save the instance to the database. Returns the instance, marked as not new 
  and perhaps trasformed by callbacks."
  [instance]
  (let [validated (validations/validated instance)]
    (if (validations/valid? validated)
      (if-let [before-saved (callbacks/before-save-callbacked validated)]
        (let [persisted (persist before-saved)
              after-saved (callbacks/after-save-callbacked persisted)]
          after-saved))
      validated)))

(defn destroy
  [instance])