(in-ns 'stash.core)

(defn reporter-fn
  "Helper fn to bridge the logger and reporter interfaces."
  [logger]
  (if (and logger ((:test logger) :info))
    (fn [sql time]
      ((:log logger) (if time (str "query: (" time " msecs) " sql) sql)))))

(defmacro with-logger
  "Execute body in a context where the clj-jdbc reporter is bound to invoke
  the logger."
  [logger-form & body]
  `(let [reporter# (reporter-fn ~logger-form)]
     (jdbc/with-reporter reporter#
       ~@body)))

(defn execute
  "Execute a clj-jdbc query function with a connection to data-source using the
  given sql. Log to the logger if given."
  [execute-fn data-source sql & [logger]]
  (with-logger logger
    (jdbc/with-connection data-source
      (execute-fn sql))))

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

(defn- pk-where-exp
  "Returns the where exp data structure that can be used in a query to identify
  the instance row, based on its pks."
  ([pk-names pk-vals]
   (if (= 1 (count pk-names))
     [(first pk-names) := (first pk-vals)]
     (reduce
       (fn [and-exp [pk-name pk-val]] (conj and-exp [pk-name := pk-val]))
       [:and]
       (zip pk-names pk-vals))))
  ([instance]
   (let [pk-names (pk-column-names (instance-model instance))
         pk-vals  (map instance pk-names)]
     (pk-where-exp pk-names pk-vals))))

(defn- pk-where-sql
  "Returns the sql where clause that identifies the instance row, based on its
   pks."
   [instance]
   (where-sql (instance-model instance) (pk-where-exp instance)))

(defn update-sql
  "Returns the update sql for the instance."
  [instance]
  (let [model        (instance-model instance)
        column-names (non-pk-column-names model)
        quoters      (quoters-by-name model)]
    (str "UPDATE " (table-name-str model) " SET "
         (str-join ", "
           (map #(str (name %) " = " ((quoters %) (instance %))) column-names))
         (pk-where-sql instance))))

(defn delete-sql
  "Returns the delete sql for the instance."
  [instance]
  (let [model (instance-model instance)]
    (str "DELETE FROM " (table-name-str model) (pk-where-sql instance))))

(defn persist-insert
  "Persists the new instance to the database, returns an instance
  that is no longer marked as new."
  [instance]
  (let [model (instance-model instance)]
    (execute jdbc/modify (data-source model)
      (insert-sql instance) (logger model)))
  (with-assoc-meta instance :new false))

(defn persist-update
  "Persists all of the instance to the database, returns the instance."
  [instance]
  (let [model (instance-model instance)]
    (execute jdbc/modify (data-source model)
      (update-sql instance) (logger model)))
  instance)

(defn delete
  "Delete the instance from the database. Returns an instance indicating that
  it has been deleted."
  [instance]
  (let [model (instance-model instance)]
    (execute jdbc/modify (data-source model)
      (delete-sql instance) (logger model)))
  (with-assoc-meta instance :deleted true))

(defn parsed-attrs
  "Returns a version of the unparsed-attrs that are parsed according to the 
  specifactions of the mdoel."
  [model unparsed-attrs]
  (let [parsers (parsers-by-name model)]
    (reduce
      (fn [parsed [name val]]
        (assoc parsed name ((parsers name) val)))
      {}
      unparsed-attrs)))

(defn cast-attrs
  "Returns a version of the uncast-attrs that are cast according to the
  specifications of the model."
  [model uncast-attrs]
  (let [casters (casters-by-name model)]
    (reduce
      (fn [cast [name val]]
        (assoc cast name ((casters name) val)))
      {}
      uncast-attrs)))

(defn update-attrs*
  "Like update-attrs, but bypasses mass-assignment protection."
  [instance attrs]
  (merge instance (cast-attrs (instance-model instance) attrs)))

(defn update-attrs
  "Returns a new instance based on the given instance but reflecting any
  attribute values in attrs. Does not touch the databse."
  [instance attrs]
  (limit-keys attrs (accessible-attrs (instance-model instance))
    "Attempted to mass-assign keys not declared as accessible-attrs: %s")
  (update-attrs* instance attrs))

(defn new-instance
  "Returns a new instance of the given model, with only the auto-initializing
  pk values set."
  [model]
  (with-meta ((new-instance-fn model)) {:model model :new true}))

(defn init*
  "Like init, but bypasses mass-assignment protection."
  [model & [attrs]]
  (update-attrs* (new-instance model) attrs))

(defn init
  "Returns an instance of the model with the given attrs having new status."
  [model & [attrs]]
  (update-attrs (new-instance model) attrs))

(defn instantiate
  "Returns an instance based on unparsed versions of the given quoted attrs 
  having non-new status. "
  [model unparsed-attrs]
  (with-meta (parsed-attrs model unparsed-attrs) {:model model}))

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
  (let [[bv-name av-name bs-name as-name persist-fn]
          (if (new? instance)
            [:before-validation-on-create :after-validation-on-create
             :before-create :after-create persist-insert]
            [:before-validation-on-update :after-validation-on-update
             :before-update :after-update persist-update])
        [bv-instance bv?] (run-named-callbacks instance bv-name)]
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
                as-instance))))))))

(defn create*
  "Like create, but bypasses mass-assignment protection."
  [model & [attrs]]
  (save (init* model attrs)))

(defn create
  "Creates an instance of the model with the attrs."
  [model & [attrs]]
  (save (init model attrs)))

(defn update*
  "Like update, but bypasses mass-assignment protection."
  [instance attrs]
  (save (update-attrs* instance attrs)))

(defn update
  "Like update-attrs, but saves the model as well."
  [instance attrs]
  (save (update-attrs instance attrs)))

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