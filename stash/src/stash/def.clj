(ns stash.def)

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
  (assoc model-hash
    :table-name-str
      (name (model-hash :table-name)))
  model-hash)

