(in-ns 'stash.core)

;; Runtime model accessors

(defn- instance-model
  "Returns the model associated with the instance, or throws if this is missing
  that metadata / is not an instance."
  [instance]
  (or (:model (meta instance))
      (throwf "No model found for %s" instance)))

(defn- model-map
  "Returns the original model map used to define the compiled model."
  [model]
  (:model-map model))

(defn- data-source
  "Returns the data source for to the model."
  [model]
  (:data-source model))

(defn- logger
  "Returns the logger associated with the model."
  [model]
  (:logger model))

(defn- table-name-str
  "Returns as a string the table name for the model."
  [model]
  (name (:table-name model)))

(defn- new-instance-fn
  "Returns a function that returns a new instance of the model when invoked,
  in paritular initiaziling the auto pk if the model has one or returning
  an empty map if it does not."
  [model]
  (:new-instance-fn model))

(defn- column-names
  "Returns as a seq of keywords the column names for the model, including pks."
  [model]
  (:column-names model))

(defn- non-pk-column-names
  "Returns as a seq of keywords the column names for the model, excluding pks."
  [model]
  (:non-pk-column-names model))

(defn- pk-column-names
  "Returns as a seq of keywords the pk column names for the model."
  [model]
  (:pk-column-names model))

(defn- quoters-by-name
  "Returns a map of keyword column names to a quoter fn for the column."
  [model]
  (:quoters-by-name model))

(defn- parsers-by-name
  "Returns a map of keyword column names to a parser fn for the column."
  [model]
  (:parsers-by-name model))

(defn- casters-by-name
  "Returns a map of keyword column names to a caster fn for the column."
  [model]
  (:casters-by-name model))

(defn- validators
  "Returns an seq of validator fns used to validate instances of the model."
  [model]
  (:validators model))

(defn- callbacks
  "Returns a map keyword callback names to a seq of corresponding callback fns."
  [model]
  (:callbacks model))

(defn- accessible-attrs
  "Returns a seq of keyword column names eligible for mass-assignement."
  [model]
  (:accessible-attrs model))


;; Initialization-time compilation helpers

(defn- checked-data-source
  [model-map]
  (get-or model-map :data-source
    (throwf ":data-source not provided in model map")))

(defn- checked-logger
  [model-map]
  (get model-map :logger))

(defn- checked-table-name
  [model-map]
  (get-or model-map :table-name
    (throwf ":table-name not provided in model map")))

(declare auto-uuid auto-integer)

(defn- compiled-new-instance-fn
  [column-defs table-name data-source logger]
  (let [auto-pk-column-defs (filter #(get-in % [2 :auto]) column-defs)
        auto-pk-count       (count auto-pk-column-defs)]
    (cond (> auto-pk-count 1)
            (throwf "More than 1 auto pk column specified.")
          (= auto-pk-count 1)
            (let [[column-name type] (first auto-pk-column-defs)]
              (cond (= type :uuid)
                      #(hash-map column-name (auto-uuid))
                    (= type :integer)
                      #(hash-map column-name
                         (auto-integer table-name column-name data-source logger))
                    :else (throwf "Unrecognized auto pk column type: %s" type)))
          :else
            hash-map)))

(defn- checked-column-defs
  [model-map]
  (get-or model-map :columns (throwf ":columns not provided in model map")))

(defn- compiled-column-names
  [column-defs]
  (map first column-defs))

(defn- compiled-pk-column-names
  [column-defs]
  (or (map first (filter #(get-in % [2 :pk]) column-defs))
      (throwf "no column defs include the :pk option")))

(defn- compiled-non-pk-column-names
  [column-names pk-column-names]
  (let [pk-cnames-set (set pk-column-names)]
    (remove pk-cnames-set column-names)))

(defn- compiled-mappers-by-name
  [mapper-finder column-defs]
  (mash
    (fn [[name type]]
      (if-let [mapper (mapper-finder type)]
        [name mapper]
        (throwf "Unrecognized column type: %s" type)))
    column-defs))

(defn- compiled-validators
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
  [model-map]
  (let [cb-map   (get model-map :callbacks)]
    (limit-keys cb-map recognized-callback-names
      "Unrecognized callback names: %s")
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
  #{:table-name :data-source :logger :pk :pks :columns
    :accessible-attrs :callbacks :validations :extensions})

(defn- checked-model-map
  [model-map]
  (limit-keys model-map recognized-model-keys
    "Unrecognized model map keys: %s"))

(defn- extended-model-map
  [model-map extensions]
  (reduce
     (fn [m extension] (checked-model-map (extension m)))
     (checked-model-map model-map)
     extensions))

(defn compiled-model
  "Returns a compiled model representation that can be used later as the 
  ubiquitious model parameter."
  [unextended-model-map]
  (let [model-map           (extended-model-map
                              (checked-model-map unextended-model-map)
                              (:extensions unextended-model-map))
        column-defs         (checked-column-defs model-map)
        column-names        (compiled-column-names column-defs)
        pk-column-names     (compiled-pk-column-names column-defs)
        table-name          (checked-table-name model-map)
        data-source         (checked-data-source model-map)
        logger              (checked-logger model-map)]
      {:table-name          table-name
       :data-source         data-source
       :logger              logger
       :column-names        column-names
       :pk-column-names     pk-column-names
       :non-pk-column-names (compiled-non-pk-column-names column-names pk-column-names)
       :new-instance-fn     (compiled-new-instance-fn column-defs table-name data-source logger)
       :quoters-by-name     (compiled-mappers-by-name type-quoter column-defs)
       :parsers-by-name     (compiled-mappers-by-name type-parser column-defs)
       :casters-by-name     (compiled-mappers-by-name type-caster column-defs)
       :validators          (compiled-validators model-map)
       :callbacks           (compiled-callbacks model-map)
       :accessible-attrs    (checked-accessible-attrs model-map)
       :model-map           model-map}))

(defmacro defmodel
  "Short for (def name (compiled-model model-map))"
  [name model-map]
  `(def ~name (compiled-model ~model-map)))
