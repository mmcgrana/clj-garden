(ns weldblog.utils
  (:use clojure.contrib.prxml))

(defn xml
  [& markup]
  (with-out-str (apply prxml markup)))