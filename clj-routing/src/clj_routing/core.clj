(ns clj-routing.core
  (:use clojure.contrib.str-utils
        clojure.contrib.def
        clojure.contrib.except
        clj-routing.utils))

(defn- route-segments
  "A seq of Strings and keywords based on the given route pattern, 
  where the strings correspond to the literal segments of the route and the 
  symbols to the named dynamic segments."
  [pattern]
  (let [split-pattern (re-partition #":[a-z-]+" pattern)
        segments (map (fn [seg-str]
                        (if (.startsWith seg-str ":")
                          (keyword (.substring seg-str 1))
                          seg-str))
                      split-pattern)]
    (if (= "" (first segments)) (rest segments) segments)))

(defn- route-generator
  "Returns a route generator fn that will take params and return a
  [method path unused-params] tuple."
  [method pattern]
  (let [segments     (route-segments pattern)
        sym-segments (filter keyword? segments)]
    (fn [params]
      (let [path (map-str
                   (fn [seg]
                     (if (keyword? seg)
                        (get-or params seg (throwf "Missing param: %s" seg))
                        seg))
                   segments)
            unused-params (apply dissoc params sym-segments)]
        [method path unused-params]))))

(defvar- standard-condition #"([a-zA-Z0-9-_]+)")

(defn- route-recognizer
  "Returns a route recognizer fn that will take a path and return nil if the
  path does not match or a [name params] tuple if it does."
  [name pattern conditions]
  (let [segments     (route-segments pattern)
        sym-segments (filter keyword? segments)
        regexp       (re-pattern
                       (map-str
                         (fn [seg]
                           (if (keyword? seg)
                             (if-let [user-condition (get conditions seg)]
                               (str "(" user-condition ")")
                               standard-condition)
                             seg))
                         segments))]
    (fn [path]
      (let [matcher (re-matcher regexp path)]
        (if (.matches matcher)
          [name (zipmap sym-segments (rest (re-groups matcher)))])))))

(defn compile-generator
  "Given a seq-able of route definitions, returns a function that takes a name 
  and params and returns a [method path unused-params] tuple."
  [routes]
  (let [route-generators-by-name
         (mash
           (fn [[name method pattern conditions]]
             (let [gen-method (if (= method :any) :get method)]
               [name (route-generator gen-method pattern)]))
           routes)]
    (fn [name & [params]]
       (if-let [named-generator (route-generators-by-name name)]
        (named-generator params)
        (throwf "Unrecognized route name: %s" name)))))

(defn compile-recognizer
  "Given a seq-able of route definitions, returns a function that takes a method 
  and path and returns a [name params] tuple."
  [routes]
  (let [any-expanded-routes
          (mapcat
            (fn [[name method pattern conditions :as tuple]]
              (if (= method :any)
                (map #(assoc tuple 1 %1) '(:get :head :options :post :put :delete))
                [tuple]))
            routes)
        route-recognizers-by-method
          (mash-grouped
            (fn [[name method pattern conditions]]
              [method (route-recognizer name pattern conditions)])
            any-expanded-routes)]
    (fn [method path]
      (if-let [method-recognizers (route-recognizers-by-method method)]
        (if-let [recognized (some #(% path) method-recognizers)]
          recognized
          (throwf "Unrecognized path: %s for method: %s" path method))
        (throwf "Unrecognized method: %s" method)))))