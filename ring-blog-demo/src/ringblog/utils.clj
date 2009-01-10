(ns ringblog.utils)

(defn xml
  [& markup]
  (with-out-str (apply prxml markup)))

(defn flashing
  [key response-tuple]
  )