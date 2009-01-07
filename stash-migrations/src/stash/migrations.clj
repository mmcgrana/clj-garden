(ns stash.migrations
  (:use (clojure.contrib str-utils def))
  (:require [clj-jdbc.core :as jdbc]
            [stash.core    :as stash])
  (:load "migrations_ddl"))

(defn create-version
  "Create a schema_info table conn's db with a single column 'version' and
  create a single row with 'version' set to 0."
  [conn]
  (jdbc/modify conn "CREATE TABLE schema_info (version int8)")
  (jdbc/modify conn "INSERT INTO schema_info (version) VALUES (0)"))

(defn drop-version
  "Drop the schema_info table from conn's db."
  [conn]
  (jdbc/modify conn "DROP TABLE schema_info"))

(defn get-version
  "Returns an int for version stored in the schema_info table for conn's db."
  [conn]
  (jdbc/select-value conn "SELECT version FROM schema_info"))

(defn set-version
  "Update the version int in the schema_info table for conn's db."
  [conn version]
  (jdbc/modify conn (str "UPDATE schema_info SET version = " version)))

(defn ups
  "Returns the subsequence of migrations needed to migrate a db up from
  start-version to target-version."
  [migrations start-version target-version]
  (map (fn [[version f]] [version f version])
       (take-while #(<= (first %) target-version)
                   (drop-while #(<= (first %) start-version) migrations))))

(defn- down-to-map
  [migrations]
  (reduce (fn [m [from to]] (assoc m from to)) {}
          (partition 2 1 (reverse (cons 0 (map first migrations))))))

(defn downs
  "Returns the subsequence of migrations needed to migrate a db down from
  start-version to target-version. Note that the returned subseqeunce is
  reversed as is appropriate."
  [migrations start-version target-version]
  (let [dto-map (down-to-map migrations)]
    (map (fn [[version f to]] [version f (dto-map to)])
         (reverse (ups migrations target-version start-version)))))

(defn- report-if [reporter message]
  (if reporter (reporter message)))

(defn migrate
  "Migrate conn's db according to the given migrations sequence up or down
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
  
  If a reporter fn is given, is will be invoked with single string arguments
  indicating the progress of the migrations."
  [conn migrations & [target-version reporter]]
  (let [start-version  (get-version conn)
        target-version (or target-version (first (last migrations)))
        report         (partial report-if reporter)]
    (cond
      ; Migrate up.
      (< start-version target-version)
        (do
          (report (str "migrating up, " start-version " to " target-version))
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
          (report (str "migrating down, " start-version " to " target-version))
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
          (report (str "migrating not needed, at " target-version))))))

(defmacro defmigration [name [binding-sym version] up-form down-form]
  "Helper method to define named migrations.

  The vars def'd by this macro should then be collected into a sequence
  to be given as an argument to stash.migrations/migrate."
  `(def ~name
     [~version
      (fn [~binding-sym] ~up-form)
      (fn [~binding-sym] ~down-form)]))