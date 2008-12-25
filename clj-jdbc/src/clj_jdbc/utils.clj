(ns clj-jdbc.utils)

(defmacro returning
  [val-form & body]
  `(let [return# ~val-form]
     ~@body
     return#))