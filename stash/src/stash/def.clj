(ns stash.def)

(defn- instance-model
  [instance]
  (or (:model (meta instance))
      (throwf "No model found for %" instance)))

(defn- instance-data-source
  [instance]
  (or (:data-source (:model (meta instance))))

(defn- column-names
  [model]
  (:column-names model))

(defn- table-name-str
  [model]
  (name (:table-name model)))

(defn- quoter-by-name
  [model]
  (:quoter-by-name model))

(defn- caster-by-name
  [model]
  (:caster-by-name model))

(defn defmodel
  "Define a model according the given model-hash specification. Returns a
  representation that can be used later as the ubiquitious model parameter.
  
  {:table-name  <keyword>
   :data-source <data-source>
   :columns
     [[<name-keyword> <type-keyword> <options-map?>]
      [<name-keyword> <type-keyword> <options-map?>]
      ...]
   :callbacks
     {:before-validation-on-create
        [:create-permalink]
      :after-save
        [:email-admin :log-save]}}"
  ; need to fill in missing callbacks, do callback proxying.
  ; :if and :unless for callbacks
  [model-hash]
  TODO)

