(ns weldblog.utils
  (:use clojure.contrib.prxml (clj-html core utils)))

(defmacro xml
  [& body]
  `(with-out-str (prxml ~@body)))

(defmacro defxml
  [name args & body]
  `(defn ~name ~args (xml ~@body)))

(defn to-int [x]
  (try (Integer. x) (catch Exception e nil)))

(defmacro let-html
  "Like let, but applying the html macro to the body."
  [bindings & body]
  `(let ~bindings (html ~@body)))