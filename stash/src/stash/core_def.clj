(in-ns 'stash.core)

(defn- instance-model
  "Returns the model associated with the instance, or throws if this is missing
  that metadata."
  [instance]
  (or (:model (meta instance))
      (throwf "No model found for %" instance)))

(defn- data-source
  [model]
  (:data-source model))

(defn- instance-data-source
  [instance]
  (data-source (instance-model instance)))

(defn- table-name-str
  [model]
  (name (:table-name model)))

(defn- column-names
  [model]
  (:column-names model))

(defn- column-names-sans-id
  [model]
  (:column-names-sans-id model))

(defn- quoters-by-name
  [model]
  (:quoters-by-name model))

(defn- casters-by-name
  [model]
  (:casters-by-name model))

(defn- validations
  [model]
  (:validations model))

(defn- callbacks
  [model]
  (:callbacks model))

(defn- check-data-source
  [model-map]
  (get-or model-map :data-source
    (throwf ":data-source not provided in model map")))

(defn- check-table-name
  [model-map]
  (get-or model-map :table-name
    (throwf ":table-name not provided in model map")))

(defn- check-column-defs
  [model-map]
  (get-or model-map :columns
    (throwf ":columns not provided in model map")))

(defn- compile-column-names-sans-id
  "Returns a seq of non-:id column names based on column-defs."
  [column-defs]
  (map first column-defs))

(defn- compile-column-names
  "Returns a seq of column names based on column-defs that additionally
  includes :id."
  [column-defs]
  (cons :id (compile-column-names-sans-id column-defs)))

(defn- compile-mappers-by-name
  "TODOC"
  [mapper-finder column-defs]
  (assoc
    (mash (fn [[name type]] [name (mapper-finder type)]) column-defs)
    :id (mapper-finder :uuid)))

(defn- check-validations
  "Returns a integrity-checked validations map based on model-map."
  [model-map]
  (:validations model-map))

(def- recognized-callbacks
  #{:before-validation-on-create :before-validation
    :after-validation-on-create :after-validation
    :before-create :before-save :after-save :after-create
    :before-validation-on-update :before-validation
    :after-validation-on-update :after-validation
    :before-update :before-save :after-save :after-update
    :before-destroy :after-destroy})

(defn- compile-callbacks
  "Returns a normalized map of callback names to callback fn colls.
  Raises on any unrecognized callback names."
  [model-map]
  (let [cb-map   (get model-map :callbacks)
        cb-keys  (set (keys cb-map))
        bad-keys (difference cb-keys recognized-callbacks)]
    (if (not (empty? bad-keys))
      (throwf "Unrecognized callback names %s" bad-keys)
      {:before-validation-on-create
         (concat (get cb-map :before-validation-on-create)
                 (get cb-map :before-validation))
       :after-validation-on-create
         (concat (get cb-map :after-validation)
                 (get cb-map :after-validation-on-create))
       :before-create
         (concat (get cb-map :before-create) (get cb-map :before-save))
       :after-create
         (concat (get cb-map :after-save) (get cb-map :after-create))
       :before-validation-on-update
         (concat (get cb-map :before-validation-on-update)
                 (get cb-map :before-validation))
       :after-validation-on-update
         (concat (get cb-map :after-validation)
                 (get cb-map :after-validation-on-update))
       :before-update
         (concat (get cb-map :before-update) (get cb-map :before-save))
       :after-update
         (concat (get cb-map :after-save) (get cb-map :after-update))
       :before-destroy
         (get cb-map :before-destroy)
       :after-destroy
         (get cb-map :after-destroy)})))

(defn compiled-model
  "Define a model according the given model-map specification. Returns a
  representation that can be used later as the ubiquitious model parameter.
  
  {:table-name  :posts
   :data-source <#DataSource>
   :columns
     [[:title      :string]
      [:view-count :integer]
      ...]
   :callbacks
     {:before-validation-on-create
        [create-permalink]
      :after-save
        [email-admin log-save]}
   :validations
     [(presence :title)]}"
  [model-map]
  (let [column-defs (check-column-defs model-map)]
    {:table-name           (check-table-name model-map)
     :data-source          (check-data-source model-map)
     :column-names-sans-id (compile-column-names-sans-id column-defs)
     :column-names         (compile-column-names column-defs)
     :quoters-by-name      (compile-mappers-by-name column-quoter column-defs)
     :casters-by-name      (compile-mappers-by-name column-caster column-defs)
     :validations          (check-validations model-map)
     :callbacks            (compile-callbacks model-map)}))

(defmacro defmodel
  "Short for (def name (compiled-model model-map))"
  [name model-map]
  `(def ~name (compiled-model ~model-map)))