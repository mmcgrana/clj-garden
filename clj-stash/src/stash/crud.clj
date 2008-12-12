(ns stash.crud
  (:require [clj-jdbc.core :as jdbc])
  (:use stash.utils))

; qutoe
; columns => :str-name :name :quoter
; :id

(defn quote
  [column value]
  ((:quoter column) value))

(defn insert-sql
  [instance]
  (let [model (:model (meta instance))
        columns (:columns model)]
    (str "INSERT INTO " (:table-name model) " "
         "(" (str-join ", " (map :str-name columns)) ") "
         "VALUES "
         "(" (str-join ", "
               (map (fn [column]
                       (quote column ((:name column) instance)))
                    columns) ")")))

(defn update-sql
  [instance]
  (let [model (:model (meta instance))]
    (str "UPDATE " (:table-name model) " SET "
         (str-join ","
           (map (fn [column]
                  (str (:str-name column) " = "
                       (quote column ((:name column) instance))))
                (:columns model))) " "
         "WHERE id = '" (:id instance) "'")))

(defn delete-sql
  [instance]
  (let [model (:model (meta instance))]
    (str "DELETE FROM " (:table-name model) " "
         "WHERE id = '" (:id instance) "'")))

(defn persist-insert
  [instance]
  (let [sql (insert-sql instance)]
    (do
      (jdbc/with-connection [conn (:data-source (:model (meta instance)))]
        (jdbc/modify conn sql))
      (with-updated-meta instance {:new false}))))

(defn persist-update
  [instance]
  (let [sql (update-sql instance)]
    (do
      (jdbc/with-connection [conn (:data-source (:model (meta instance)))]
        (jdbc/modify conn sql)))))

(defn persist
  [instance]
  ((if (new? instance) persist-insert persis-update) instance))

(defn delete
  [instance]
  (let [sql (delete-sql instance)]
    (do
      (jdbc/with-connection [conn (data-source instance)]
        (jdbc/modify conn sql))
      (with-updated-meta instance {:deleted true}))))

(defn new
  [model attrs]
  (with-meta {:attrs attrs :id (uuid/gen)} ))

(defn instantiate
  [model attrs])

(defn create
  [model attrs])

(defn form-create
  [model form-attrs])

(defn new?
  [instance]
  (:new (meta instance)))

(defn save
  [instance]
  (let [validated (validations/validated instance)]
    (if (validations/valid? validated)
      (if-let [before-saved (callbacks/before-save-callbacked validated)]
        (let [persisted (persist before-saved)
              after-saved (callbacks/after-save-callbacked persisted)]
          after-saved))
      validated)))

(defn save
  [instance])

(defn destroy
  [instance])