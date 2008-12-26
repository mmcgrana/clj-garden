(in-ns 'stash.core)

(defn insert-sql
  "Returns the insert sql for the instance."
  [instance]
  (let [model         (instance-model instance)
        quoters       (quoters-by-name model)
        column-names  (column-names model)]
    (str "INSERT INTO " (table-name-str model) " "
         "(" (str-join ", " (map #(name %) column-names)) ") "
         "VALUES "
         "(" (str-join ", "
               (map #((quoters %) (instance %)) column-names)) ")")))

(defn update-sql
  "Returns the update sql for the instance."
  [instance]
  (let [model        (instance-model instance)
        column-names (column-names-sans-id model)
        quoters      (quoters-by-name model)]
    (str "UPDATE " (table-name-str model) " SET "
         (str-join ", "
           (map #(str (name %) " = " ((quoters %) (instance %))) column-names))
           " WHERE id = " ((quoters :id) (instance :id)))))

(defn delete-sql
  "Returns the delete sql for the instance."
  [instance]
  (let [model (instance-model instance)]
    (str "DELETE FROM " (table-name-str model) " "
         "WHERE id = '" (:id instance) "'")))

(defn persist-insert
  "Persists the new instance to the database, returns an instance
  that is no longer marked as new."
  [instance]
  (let [sql (insert-sql instance)]
    (jdbc/with-connection [conn (instance-data-source instance)]
      (jdbc/modify conn sql))
    (with-assoc-meta instance :new false)))

(defn persist-update
  "Persists all of the instance to the database, returns the instance."
  [instance]
  (let [sql (update-sql instance)]
    (jdbc/with-connection [conn (instance-data-source instance)]
      (jdbc/modify conn sql))
    instance))

(defn delete
  "Delete the instance from the database. Returns an instance indicating that
  it has been deleted."
  [instance]
  (let [sql (delete-sql instance)]
    (jdbc/with-connection [conn (instance-data-source instance)]
      (jdbc/modify conn sql)
      (with-assoc-meta instance :deleted true))))

(defn init
  "Returns an instance of the model with the given attrs having new status."
  [model & [attrs]]
  (with-meta (assoc attrs :id (gen-uuid)) {:model model :new true}))

(defn cast-attrs
  "Returns a version of the uncast-attrs cast according to the specifactions
  of the mdoel."
  [model uncast-attrs]
  (let [parsers (parsers-by-name model)]
    (reduce
      (fn [cast-attrs [name val]]
        (assoc cast-attrs name ((parsers name) val)))
      {}
      uncast-attrs)))

(defn instantiate
  "Returns an instance based on cast versions of the given quoted attrs having 
  non-new status. "
  [model uncast-attrs]
  (with-meta (cast-attrs model uncast-attrs) {:model model} ))

(defn new?
  "Returns true if the instance has not been saved to the database."
  [instance]
  (:new (meta instance)))

(defn deleted?
  "Returns true if the instance has been deleted form the database."
  [instance]
  (:deleted (meta instance)))

(defn save
  "Save the instance to the database. Returns the instance, marked as not new 
  and perhaps trasformed by callbacks."
  [instance]
  (let [new        (new? instance)
        bv-name    (if new :before-validation-on-create
                           :before-validation-on-update)
        av-name    (if new :after-validation-on-create
                           :after-validation-on-update)
        bs-name    (if new :before-create :before-update)
        as-name    (if new :after-create  :after-update)
        persist-fn (if new persist-insert persist-update)]
    (let [[bv-instance bv?] (run-named-callbacks instance bv-name)]
      (if-not bv?
        bv-instance
        (let [v-instance (validated bv-instance)]
          (if (errors? v-instance)
            v-instance
            (let [[av-instance av?] (run-named-callbacks v-instance av-name)
                  [bs-instance bs?] (run-named-callbacks av-instance bs-name)]
              (if-not bs?
                bs-instance
                (let [s-instance (persist-fn bs-instance)
                      [as-instance as?] (run-named-callbacks s-instance as-name)]
                  as-instance)))))))))

(defn create
  "Creates an instance of the model with the attrs. Validations and validations
  and create callbacks."
  [model attrs]
  (save (init model attrs)))

(defn update-attrs
  "Returns a new instance based on the given instance but reflecting any
  attribute values in attrs. Does not touch the databse."
  [instance attrs]
  (merge instance attrs))

(defn destroy
  "Deletes the instance, running before- and after- destroy callbacks.
   Returns the instance, which is marked as deleted if appropriate."
  [instance]
  (let [[bd-instance bd?] (run-named-callbacks instance :before-destroy)]
    (if-not bd?
      bd-instance
      (let [d-instance (delete bd-instance)
           [ad-instance ad?] (run-named-callbacks d-instance :after-destroy)]
        ad-instance))))