(ns stash.callbacks)

(defn named-callbacks
  "Returns the coll of callbacks for the instance associated with the name, or
  raises if the name is unrecognized."
  [callback-name instance]
  (or (callback-name (:callbacks (instance-model instance)))
      (throwf "Unrecognized callback-name %s", callback-name)))

(defn run-callbacks
  "Run the named callbacks against the given instance. Each callback is run in
  turn; the callback should return a [transformed success] pair. If
  success is not logically true the callback chain stops and that transformed
  instance is returned, otherwise the transformed instance is used as input for 
  the next callback in the chain. If all callbacks succeed, returns the final
  transformed instance"
  [callback-name instance]
  (let [callbacks (named-callbacks callback-name instance)]
    (loop [instance instance callbacks callbacks]
      (if-let [callback (first callbacks)]
        (let [[c-instance success] (callback instance)]
          (if success
            (recur c-instance (rest callbacks))
            c-instance)))
        instance))))
