(in-ns 'stash.core)

(defmacro transaction
  "Execute the body within the scope of a transaction on an existing or new
  database connection appropriate for the model."
  [model & body]
  `(with-logger (:logger ~model)
     (jdbc/with-connection (:data-source ~model)
       (jdbc/in-transaction
         ~@body))))
