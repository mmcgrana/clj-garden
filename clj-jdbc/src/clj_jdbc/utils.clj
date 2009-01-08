(ns clj-jdbc.utils)

(defmacro returning
  [val-form & body]
  `(let [return# ~val-form]
     ~@body
     return#))

(defmacro realtime
  [expr]
  `(let [start# (System/currentTimeMillis)]
     ~expr
     (- (System/currentTimeMillis) start#)))

(defmacro realtimed
  [expr]
  `(let [start# (System/currentTimeMillis)
         ret#    ~expr
         time#  (- (System/currentTimeMillis) start#)]
     [ret# time#]))

(defmacro with-realtime
  [[binding-sym expr] form]
  `(let [[ret# ~binding-sym] (realtimed ~expr)]
     ~form
     ret#))
