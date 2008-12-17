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

(def privates
  {:callbacks callbacks})

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
  [column-defs]
  (map first column-defs))

(defn- compile-column-names
  [column-defs]
  (cons :id (compile-column-names-sans-id column-defs)))

(defn- compile-mappers-by-name
  [column-defs quoter-or-caster]
  (let [mapper
          (mash
            (fn [[name type]]
              [name (quoter-or-caster (column-mapper type))])
            column-defs)]
    (assoc mapper :id (quoter-or-caster (column-mapper :string)))))

(defn- compile-quoters-by-name
  [column-defs]
  (compile-mappers-by-name column-defs :quoter))

(defn- compile-casters-by-name
  [column-defs]
  (compile-mappers-by-name column-defs :caster))

(defn- check-validations
  [model-map]
  (:validations model-map))

(defn- compile-callbacks
  [cbs]
  {:before-validation-on-create
     (concat (get cbs :before-validation-on-create)
             (get cbs :before-validation))
   :after-validation-on-create
     (concat (get cbs :after-validation)
             (get cbs :after-validation-on-create))
   :before-create
     (concat (get cbs :before-create) (get cbs :before-save))
   :after-create
     (concat (get cbs :after-save) (get cbs :after-create))
   :before-validation-on-update
     (concat (get cbs :before-validation-on-update)
             (get cbs :before-validation))
   :after-validation-on-update
     (concat (get cbs :after-validation)
             (get cbs :after-validation-on-update))
   :before-update
     (concat (get cbs :before-update) (get cbs :before-save))
   :after-update
     (concat (get cbs :after-save) (get cbs :after-update))
   :before-destroy
     (get cbs :before-destroy)
   :after-destroy
     (get cbs :after-destroy)})

(defn compile-model
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
     :quoters-by-name      (compile-quoters-by-name column-defs)
     :casters-by-name      (compile-casters-by-name column-defs)
     :validations          (check-validations model-map)
     :callbacks            (compile-callbacks model-map)}))

