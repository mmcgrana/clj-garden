(in-ns 'stash.core)

(defn named-cbs
  "Returns the coll of callbacks for the instance associated with the name."
  [instance callback-name]
  (callback-name (callbacks (instance-model instance))))

(defn run-cbs
  "Run the callbacks against the given instance. Each callback is run in
  turn; the callback should return a [transformed success] pair. If
  success is not logically true the callback chain stops and that transformed
  instance is returned, otherwise the transformed instance is used as input for 
  the next callback in the chain. If all callbacks succeed, returns the final
  transformed instance"
  [instance callbacks]
  (loop [instance instance callbacks callbacks]
    (if-let [callback (first callbacks)]
      (let [[c-instance success] (callback instance)]
        (if success
          (recur c-instance (rest callbacks))
          c-instance))
      instance)))

(defn run-named-cbs
  "Runs the callbacks using run-cbs associated with the instance as 
  defined by named-cbs."
  [instance callback-name]
  (run-cbs instance (named-cbs instance callback-name)))


