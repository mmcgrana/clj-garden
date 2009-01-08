(in-ns 'stash.core)

;; Runtime model accessors

(defn instance-model
  "Returns the model associated with the instance, or throws if this is missing
  that metadata / is not an instance."
  [instance]
  (or (:model (meta instance))
      (throwf "No model found for %s" instance)))

(defn model-map
  "Returns the original model map used to define the compiled model."
  [model]
  (:model-map model))

(defn data-source
  "Returns the data source for to the model."
  [model]
  (:data-source model))

(defn logger
  "Returns the logger associated with the model."
  [model]
  (:logger model))

(defn table-name-str
  "Returns as a string the table name for the model."
  [model]
  (name (:table-name model)))

(defn pk-init
  "Returns, if applicable, a function returning a map of initialized primary
  key name/value pairs"
  [model]
  (:pk-init model))

(defn column-names
  "Returns as a seq of keywords the column names for the model, including pks."
  [model]
  (:column-names model))

(defn non-pk-column-names
  "Returns as a seq of keywords the column names for the model, excluding pks."
  [model]
  (:non-pk-column-names model))

(defn pk-column-names
  "Returns as a seq of keywords the pk column names for the model."
  [model]
  (:pk-column-names model))

(defn quoters-by-name
  "Returns a map of keyword column names to a quoter fn for the column."
  [model]
  (:quoters-by-name model))

(defn parsers-by-name
  "Returns a map of keyword column names to a parser fn for the column."
  [model]
  (:parsers-by-name model))

(defn casters-by-name
  "Returns a map of keyword column names to a caster fn for the column."
  [model]
  (:casters-by-name model))

(defn validators
  "Returns an seq of validator fns used to validate instances of the model."
  [model]
  (:validators model))

(defn callbacks
  "Returns a map keyword callback names to a seq of corresponding callback fns."
  [model]
  (:callbacks model))

(defn accessible-attrs
  "Returns a seq of keyword column names eligible for mass-assignement."
  [model]
  (:accessible-attrs model))


;; Initialization-time compilation helpers

(defn- checked-data-source
  "Returns a data source specified by model-map, or throws if missing."
  [model-map]
  (get-or model-map :data-source
    (throwf ":data-source not provided in model map")))

(defn- checked-logger
  [model-map]
  "Returns a logger specified by model-map, if present."
  (get model-map :logger))

(defn- checked-table-name
  "Returns a keyword for the table name specified by model-map, or thorws if
  missing."
  [model-map]
  (get-or model-map :table-name
    (throwf ":table-name not provided in model map")))

(defn- checked-pk-init
  "Returns a fn for pk initialization specified by model-map, or throws if
  it is present but is not a proper function."
  [model-map]
  (if-let [init-fn (get model-map :pk-init)]
    (if-not (fn? init-fn)
      (throwf ":init-fn given but not a function")
      init-fn)))

(defn- checked-column-defs
  "Returns a validated seq of column defs specified by model-map."
  [model-map]
  (get-or model-map :columns (throwf ":columns not provided in model map")))

(defn- compiled-column-names
  "Returns a seq of column names based on column-defs that includes all column
  names."
  [column-defs]
  (map first column-defs))

(defn- compiled-pk-column-names
  "Returns a seq of pk column names based on model-map."
  [column-defs]
  (or (map first (filter #(get-in % [2 :pk]) column-defs))
      (throwf "no column defs include the :pk option")))

(defn- compiled-non-pk-column-names
  "Returns a seq of non-pk column names based on a seq of all column name and of
  the pk-column names."
  [column-names pk-column-names]
  (let [pk-cnames-set (set pk-column-names)]
    (remove pk-cnames-set column-names)))

(defn- compiled-mappers-by-name
  "Returns a map of column name keywords to either quoter or parser fns,
  where which is specified by the mapper-finder fn."
  [mapper-finder column-defs]
  (mash
    (fn [[name type]]
      (if-let [mapper (mapper-finder type)]
        [name mapper]
        (throwf "Unrecognized column type: %s" type)))
    column-defs))

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

(defn- checked-accessible-attrs
  [model-map]
  (:accessible-attrs model-map))

(def- recognized-model-keys
  #{:table-name :data-source :logger :pk :pks :pk-init :columns
    :accessible-attrs :callbacks :validations :extensions})

(defn- checked-model-map
  "Returns the given model map provided that it contains only valid keys,
  raises otherwise."
  [model-map]
  (limit-keys model-map recognized-model-keys))

(defn compiled-model
  "Returns a compiled model representation that can be used later as the 
  ubiquitious model parameter."
  [unextended-model-map]
  (let [model-map
          (reduce
            (fn [m extension] (checked-model-map (extension m)))
            (checked-model-map unextended-model-map)
            (:extensions unextended-model-map))]
    (let [column-defs         (checked-column-defs model-map)
          column-names        (compiled-column-names column-defs)
          pk-column-names     (compiled-pk-column-names column-defs)]
      {:table-name          (checked-table-name model-map)
       :data-source         (checked-data-source model-map)
       :logger              (checked-logger model-map)
       :pk-init             (checked-pk-init model-map)
       :column-names        column-names
       :pk-column-names     pk-column-names
       :non-pk-column-names (compiled-non-pk-column-names column-names pk-column-names)
       :quoters-by-name     (compiled-mappers-by-name type-quoter column-defs)
       :parsers-by-name     (compiled-mappers-by-name type-parser column-defs)
       :casters-by-name     (compiled-mappers-by-name type-caster column-defs)
       :validators          (compiled-validators model-map)
       :callbacks           (compiled-callbacks model-map)
       :accessible-attrs    (checked-accessible-attrs model-map)
       :model-map           model-map})))

(defmacro defmodel
  "Short for (def name (compiled-model model-map))"
  [name model-map & [options]]
  (limit-keys options '(:accessors))
  (if (get options :accessors)
    `(do (def ~name (compiled-model ~model-map))
       ((resolve 'stash.core/define-accessors) ~name))
    `(def ~name (compiled-model ~model-map))))
