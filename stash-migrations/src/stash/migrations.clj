(ns stash.migrations
  (:use (clojure.contrib str-utils def))
  (:require [clj-jdbc.core :as jdbc]
            [stash.core    :as stash])
  (:load "migrations_ddl"))

(defn ensure-version
  "Create a schema_info table with a single column 'version' and a single row 
  with 'version' set to 0."
  []
  (try
    (jdbc/select-value "SELECT version FROM schema_info")
    (catch Exception e
      (jdbc/modify "CREATE TABLE schema_info (version int8)")
      (jdbc/modify "INSERT INTO schema_info (version) VALUES (0)"))))

(defn drop-version
  "Drop the schema_info table."
  []
  (jdbc/modify "DROP TABLE schema_info"))

(defn get-version
  "Returns an int for version stored in the schema_info table."
  []
  (jdbc/select-value "SELECT version FROM schema_info"))

(defn set-version
  "Update the version int in the schema_info table."
  [version]
  (jdbc/modify (str "UPDATE schema_info SET version = " version)))

(defn ups
  [migrations start-version target-version]
  (map (fn [[version up-f down-f]] [version up-f version])
       (take-while #(<= (first %) target-version)
                   (drop-while #(<= (first %) start-version) migrations))))

(defn- down-to-map
  [migrations]
  (reduce
    (fn [m [from to]] (assoc m from to))
    {}
    (partition 2 1 (reverse (cons 0 (map first migrations))))))

(defn downs
  [migrations start-version target-version]
  (let [dto-map (down-to-map migrations)]
    (map (fn [[version up-f down-f]] [version down-f (dto-map version)])
         (take-while #(> (first %) target-version)
                     (drop-while #(> (first %) start-version)
                                 (reverse migrations))))))

(defn migrate
  "Migrate a db according to the given migrations sequence up or down
  to the target version.
  The migrations argument is a coll of 3-tuples, where the first element of the
  tuple is the version number, the second a function of a conn argument
  that exectues the migrations needed to go from the previous version number
  to that version number, and the third a function of a conn argument
  that executes the migrations needed to go from that version number to
  the previous version number, i.e. to undo the migrations from the first fn.

  Note that invoking this method can cause either upward or downward migrations.
  If the current version is equal to the target version, no migrations take
  place.
  
  After each individual migration, the schema_info table is updated
  appropriately.
  
  The clj-jdbc reporter, if bound, is used to report the progress."
  [migrations & [target-version]]
  (let [start-version  (get-version)
        target-version (or target-version (first (last migrations)))
        rep            #(jdbc/report-db (str "migrations: " %))]
    (cond
      ; Migrate up.
      (< start-version target-version)
        (let [up-tuples (ups migrations start-version target-version)]
          (rep (str "migrating up, " start-version " to " target-version))
          (doseq [[version up-f to] up-tuples]
            (rep (str "running " version " up"))
            (up-f)
            (set-version to))
          (rep (str "done, at " target-version)))
      ; Migrate down.
      (> start-version target-version)
        (let [down-tuples (downs migrations start-version target-version)]
          (rep (str "migrating down, " start-version " to " target-version))
          (doseq [[version down-f to] down-tuples]
            (rep (str "running " version " down"))
            (down-f)
            (set-version to))
          (rep (str "done, at " target-version)))
      ; Dont' migrate.
      :else
        (rep (str "migrating not needed, at " target-version)))))

(defn migrate-with [migrations version data-source logger]
  "A wrapper around the stash.migrations/migrate, performers migrations in the
  context of a given data source and logger."
  (stash.core/with-logger logger
    (jdbc/with-connection data-source
      (ensure-version)
      (migrate migrations version))))

(defmacro defmigration [name version up-form down-form]
  "Helper method to define named migrations.

  The vars def'd by this macro should then be collected into a sequence
  to be given as an argument to stash.migrations/migrate."
  `(def ~name
     [~version
      (fn [] ~up-form)
      (fn [] ~down-form)]))