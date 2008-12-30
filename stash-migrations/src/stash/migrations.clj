(ns stash.migrations
  (:use clojure.contrib.str-utils)
  (:require [clj-jdbc.core :as jdbc]
            [stash.core    :as stash])
  (:load "migrations_ddl"))

(defn create-version
  [conn]
  (jdbc/modify conn "CREATE TABLE schema_info (version int8)")
  (jdbc/modify conn "INSERT INTO schema_info (version) VALUES (0)"))

(defn drop-version
  [conn]
  (jdbc/modify conn "DROP TABLE schema_info"))

(defn get-version
  [conn]
  (jdbc/select-value conn "SELECT version FROM schema_info"))

(defn set-version
  [conn version]
  (jdbc/modify conn (str "UPDATE schema_info SET version = " version)))

(defn ups
  [migrations start-version target-version]
  (map (fn [[version f]] [version f version])
       (take-while #(<= (first %) target-version)
                   (drop-while #(<= (first %) start-version) migrations))))

(defn- down-to-map
  [migrations]
  (reduce (fn [m [from to]] (assoc m from to)) {}
          (partition 2 1 (reverse (cons 0 (map first migrations))))))

(defn downs
  [migrations start-version target-version]
  (let [dto-map (down-to-map migrations)]
    (map (fn [[version f to]] [version f (dto-map to)])
         (reverse (ups migrations target-version start-version)))))

(defn report-if [reporter message]
  (if reporter (reporter message)))

(defn migrate
  [conn migrations & [target-version reporter]]
  (let [start-version  (get-version conn)
        target-version (or target-version (first (last migrations)))
        report         (partial report-if reporter)]
    (cond
      ; Migrate up.
      (< start-version target-version)
        (do
          (report (str "migrating up, " start-version "to" target-version))
          (let [ran (doall
                      (map (fn [[version f to]]
                             (report (str "running " version " up"))
                             (f conn) (set-version conn to)
                             version)
                           (ups migrations start-version target-version)))]
            (report (str "done, at " target-version))
            ran))
      ; Migrate down.
      (> start-version target-version)
        (do
          (report (str "migrating down, " start-version "to" target-version))
          (let [ran (doall
                      (map (fn [[version f to]]
                             (report (str "running " version " down"))
                             (f conn) (set-version conn to)
                             version)
                           (downs migrations start-version target-version)))]
            (report (str "done, at " target-version))
            ran))
      ; Dont' migrate.
      :else
        (if report
          (report (str "migrating not needed, at" target-version))))))
