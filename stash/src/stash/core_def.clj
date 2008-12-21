(in-ns 'stash.core)

;; Runtime model accessors

(defn- instance-model
  "Returns the model associated with the instance, or throws if this is missing
  that metadata / is not an instance."
  [instance]
  (or (:model (meta instance))
      (throwf "No model found for %" instance)))

(defn- model-map
  [model]
  "Returns the original model map used to define the compiled model."
  (:model-map model))

(defn- data-source
  "Returns the data source for to the model."
  [model]
  (:data-source model))

(defn- instance-data-source
  "Returns the data source for the model of the instance."
  [instance]
  (data-source (instance-model instance)))

(defn- table-name-str
  "Returns as a string the table name for the model."
  [model]
  (name (:table-name model)))

(defn- column-names
  "Returns as a seq of keywords the column names for the model, including :id."
  [model]
  (:column-names model))

(defn- column-names-sans-id
  "Returns as a seq of keywords the column names for the model, excluding :id."
  [model]
  (:column-names-sans-id model))

(defn- quoters-by-name
  "Returns a map of keyword column names to a quoter fn for the column."
  [model]
  (:quoters-by-name model))

(defn- parsers-by-name
  "Returns a map of keyword column names to a parser fn for the column."
  [model]
  (:parsers-by-name model))

(defn- validators
  "Returns an seq of validator fns used to validate instances of the model."
  [model]
  (:validators model))

(defn- callbacks
  "Returns a map keyword callback names to a seq of corresponding callback fns."
  [model]
  (:callbacks model))


;; Initialization-time compilation helpers

(defn- checked-data-source
  "Returns a data source specified by model-map, or throws if missing."
  [model-map]
  (get-or model-map :data-source
    (throwf ":data-source not provided in model map")))

(defn- checked-table-name
  "Returns a keyword for the table name specified by model-map, or thorws if
  missing."
  [model-map]
  (get-or model-map :table-name
    (throwf ":table-name not provided in model map")))

(defn- checked-column-defs
  "Returns a validated seq of column defs specified by model-map."
  [model-map]
  (get-or model-map :columns
    (throwf ":columns not provided in model map")))

(defn- compiled-column-names-sans-id
  "Returns a seq of non-:id column names based on column-defs."
  [column-defs]
  (map first column-defs))

(defn- compiled-column-names
  "Returns a seq of column names based on column-defs that additionally
  includes :id."
  [column-defs]
  (cons :id (compiled-column-names-sans-id column-defs)))

(defn- compiled-mappers-by-name
  "Returns a map of column name keywords to either quoter or parser fns,
  where which is specified by the mapper-finder fn."
  [mapper-finder column-defs]
  (assoc
    (mash (fn [[name type]] [name (mapper-finder type)]) column-defs)
    :id (mapper-finder :uuid)))

(defn- compiled-validators
  "Returns a seq of validator fns corresponding to the model's validations."
  [model-map]
  (map
    (fn [[attr-name validator-gen]] (validator-gen attr-name))
    (:validations model-map)))

(def- recognized-callback-names
  #{:before-validation-on-create :before-validation
    :after-validation-on-create :after-validation
    :before-create :before-save :after-save :after-create
    :before-validation-on-update :before-validation
    :after-validation-on-update :after-validation
    :before-update :before-save :after-save :after-update
    :before-destroy :after-destroy})

(defn- compiled-callbacks
  "Returns a normalized map of callback names to callback fn colls.
  Raises on any unrecognized callback names."
  [model-map]
  (let [cb-map   (get model-map :callbacks)]
    (limit-keys cb-map recognized-callback-names)
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
       (get cb-map :after-destroy)}))

(def- recognized-model-keys
  #{:table-name :data-source :columns :callbacks :validations})

(defn compiled-model
  "Define a model according the given model-map specification. Returns a
  representation that can be used later as the ubiquitious model parameter."
  [model-map]
  (limit-keys model-map recognized-model-keys)
  (let [column-defs (checked-column-defs model-map)]
    {:table-name           (checked-table-name model-map)
     :data-source          (checked-data-source model-map)
     :column-names-sans-id (compiled-column-names-sans-id column-defs)
     :column-names         (compiled-column-names column-defs)
     :quoters-by-name      (compiled-mappers-by-name type-quoter column-defs)
     :parsers-by-name      (compiled-mappers-by-name type-parser column-defs)
     :validators           (compiled-validators model-map)
     :callbacks            (compiled-callbacks model-map)
     :model-map            model-map}))

(defmacro defmodel
  "Short for (def name (compiled-model model-map))"
  [name model-map]
  `(def ~name (compiled-model ~model-map)))