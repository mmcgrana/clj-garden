(ns weldblog.utils)

(defn xml
  [& markup]
  (with-out-str (apply prxml markup)))