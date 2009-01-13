(ns weldup.utils
  (:use (clj-html core utils helpers-ext)))

(defmacro html-for
  [[elem-bind coll] & body]
  `(domap-str [~elem-bind ~coll]
     (html ~@body)))
