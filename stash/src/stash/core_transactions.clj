(in-ns 'stash.core)

(defmacro transaction
  "Execute the body within the scope of a transaction on an existing or new
  database connection appropriate for the model."
  [model & body]
  `(jdbc/with-connection [conn# (data-source ~model)]
     (jdbc/in-transaction conn#
       ~@body)))
