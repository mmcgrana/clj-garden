(ns ring.http-utils
  (:use (clojure.contrib def str-utils except)
        ring.utils))

(defn url-escape
  "Returns a url-escaped representation of the given String."
  [unescaped]
  (java.net.URLEncoder/encode unescaped "UTF-8"))

(defn url-unescape
  "Returns a url-unescaped representation of the given String."
  [escaped]
  (java.net.URLDecoder/decode escaped "UTF-8"))


(defvar- after-initial-key-pat #"^([^\[]*)(\[.*)$"
  "Matches keys of the form key-head([..key-trail..)")

(defvar- hash-key-pat          #"^\[([^\]]+)\](.*)$"
  "Matches keys of the form [key-head](..key-tail..)")

(defvar- nested-hash-key-pat   #"^\[\]\[([^\]]+)\](.*)$"
  "Matches keys of the form [][key-head](..key-tail..)")

; 3 Internets awarded to whoever makes this implementation smaller.
(defn- pairs-parse-nested
  [params key value]
  ; cases - "", "[]", "[foo]...", "[][foo]..."
  (if (= key "")
    value
    (if (= key "[]")
      (conj (or params []) value)
      (if-let [found (re-find hash-key-pat key)]
        (let [key-head (keyword (nth found 1))
              key-rest (nth found 2)]
          (assoc params key-head
            (pairs-parse-nested (get params key-head) key-rest value)))
        (if-let [found (re-find nested-hash-key-pat key)]
          (let [key-head   (keyword (nth found 1))
                key-rest   (nth found 2)
                last-inner (last params)]
            (cond
              (not last-inner)
                [{key-head (pairs-parse-nested nil key-rest value)}]
              (contains? last-inner key-head)
                (conj params
                  {key-head (pairs-parse-nested nil key-rest value)})
              :else
                (let [inner (assoc last-inner key-head
                              (pairs-parse-nested (get last-inner key-head)
                                key-rest value))]
                  (if (empty? params)
                    [inner]
                    (assoc params (dec (count params)) inner)))))
          (throwf "Unrecognized key: %s" key))))))

(defn pairs-parse
  "Returns a potentially-nested data structure corresponding to the given
  name, value pairs."
  [pairs]
  (let [non-empty (remove #(nil? (second %)) pairs)]
    (reduce
      (fn [params [key value]]
        (if-let [found (re-find after-initial-key-pat key)]
          (let [key-head (keyword (nth found 1))
                key-rest (nth found 2)]
            (assoc params key-head
              (pairs-parse-nested
                (get params key-head) key-rest value)))
          (assoc params (keyword key) value)))
      {}
      non-empty)))

(defn- querylike-parse
  "Helper for public facing functions parsing querystring-like values."
  [separator string]
  (if string
    (let [segments  (re-split separator string)
          unescaped (map url-unescape segments)
          pairs     (map #(re-split #"=" % 2) unescaped)]
      (pairs-parse pairs))))

(defn query-parse
  "Returns a potentially-nested data structure corresponding to the given
  query string."
  [query-string]
  (querylike-parse #"&\s*" query-string))

(defn cookie-parse
  "Returns a non-nested map of cookie values corresponding to the given Cookie
  header string, or nil if the given value is nil."
  [cookie-string]
  (querylike-parse #";\s*" cookie-string))


(defn query-unparse-line
  [line]
  (let [first-key (first line)
        rest-keys (butlast (rest line))
        value     (last (rest line))]
    (str (url-escape (name first-key))
         (str-cat (map (fn [key]
                         (if (= key []) "[]"
                           (str "[" (url-escape (name key)) "]")))
                       rest-keys))
         "=" (url-escape (str value)))))

(defn query-unparse-lines
  [lines]
  (str-join "&" (map query-unparse-line lines)))

(defn params-as-lines
  [params head]
  (cond
    (map? params)
      (mapcat (fn [[key value]] (params-as-lines value (conj head key))) params)
    (coll? params)
      (mapcat (fn [elem] (params-as-lines elem (conj head []))) params)
    :else
      [(conj head params)]))

(defn query-unparse
  "The opposite of query-parse, converts a nested data structure into a query
  string."
  [params]
  (query-unparse-lines (params-as-lines params [])))
