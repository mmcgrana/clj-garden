(ns clj-jdbc.utils)

(defmacro returning
  [val-form & body]
  `(let [return# ~val-form]
     ~@body
     return#))

(defmacro timed
  [expr]
  `(let [start# (System/currentTimeMillis)
         ret#    ~expr
         time#  (- (System/currentTimeMillis) start#)]
     [ret# time#]))