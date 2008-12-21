(in-ns 'stash.core)

; before-validation-on-create
;   validate-on-create
; after-validation-on-create
; before-create
;   create
; after-create
;
; before-validation-on-update
;   validate-on-update
; after-validation-on-update
; before-update
;   update
; after-update
;
; before-destroy
;   destroy
; after-destroy

(defn named-callbacks
  "Returns the coll of callbacks for the instance associated with the name."
  [instance callback-name]
  (callback-name (callbacks (instance-model instance))))

(defn run-callbacks
  "Run the callbacks against the given instance. Each callback is run in
  turn; the callback should return a [transformed success] pair. If
  success is not logically true the callback chain stops and that transformed
  instance is returned, otherwise the transformed instance is used as input for 
  the next callback in the chain. If all callbacks succeed, returns the final
  transformed instance"
  [instance callbacks]
  (loop [instance instance callbacks callbacks passed true]
    (if-let [callback (first callbacks)]
      (let [[c-instance success] (callback instance)]
        (if success
          (recur c-instance (rest callbacks) true)
          [c-instance false]))
      [instance true])))

(defn run-named-callbacks
  "Runs the callbacks using run-callbacks associated with the instance as 
  defined by named-callbacks."
  [instance callback-name]
  (run-callbacks instance (named-callbacks instance callback-name)))


