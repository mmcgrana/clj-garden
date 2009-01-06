; Pulling together code from clojure core, clojure contrib, snippets on the web,
; and some of my own work to create an easy-to-use and concise html scraping
; api.

(ns clj-scrape.core
  (:use clj-scrape.utils))

(require '[clojure.xml :as xml]
         '[clojure.contrib.zip-filter :as zf]
         '[clojure.contrib.zip-filter.xml :as zfxml]
         '[clojure.zip :as zip])

; http://paste.lisp.org/display/70171
(defn- startparse-tagsoup [s ch]
  "startparse fn required by xml/parse constructor for generating a tagsoup
  parse stream."
  (doto (org.ccil.cowan.tagsoup.Parser.)
      (.setContentHandler ch)
      (.parse s)))

(defn dom [stream]
  "Returns the parsed and zipped dom data strucutre that can be given as the
  first argument to xml->."
  (zip/xml-zip
    (xml/parse (org.xml.sax.InputSource. stream) startparse-tagsoup)))

(def rightmost?  zf/leftmost?)
(def rightmost?  zf/rightmost?)
(def desc        zf/descendants)
(def ansc        zf/ancestors)
(def children    zf/children)
(def node        zip/node)
(def tag=        zfxml/tag=)
(def attr        zfxml/attr)
(def attr=       zfxml/attr=)
(def text        zfxml/text)
(def text=       zfxml/text=)

(defn class= [target-name]
  "Returns a query predicate that matches a node when has a class named
  target-name."
  (let [name-re (re-pattern (str "\\b" target-name "\\b"))]
    (fn [loc] (if-let [class-str (attr loc :class)]
                (re-match? name-re class-str)))))

(defn id= [target-id]
  "Returns a query predicate that matches a node when it has an id target-id."
  (fn [loc]
    (when-let [id (zfxml/attr loc :id)]
      (= id target-id))))

(defn attrs= [attrs-map]
  "Returns a query predicate like attr= but that works on multiple attr 
  name/value pairs."
  (let [conditions (map (fn [[name val]] (attr= name val)) attrs-map)]
    (fn [loc] (every? #(% loc) conditions))))

(defn attr? [attrname]
  "Returns a query predicate that matches a node when it has the attribute
  attrname."
  (fn [loc] (contains? (-> loc zip/node :attrs) attrname)))

(defn attr-match? [attrname re]
  "Returns a query predicate that matches a node when its attribute value for
  attrname matches re."
  (fn [loc] (if-let [attr-str (zfxml/attr loc attrname)]
              (re-match? re attr-str))))

(defn nth-elem [n]
  "Returns a query filter that will return the the nth node in a seq 
  (1 indexed)."
  (fn [loc] (nth (iterate zip/right loc) (dec n))))

(defn text-match?
  "Returns a query predicate that will return if the node has text matching
  the re."
  [re]
  (fn [loc] (re-match? re (text loc))))

; patched version of xml-> from contrib adding literal attr and text matching.
(defn xml->
  [loc & preds]
  (zf/mapcat-chain loc preds
                   #(cond (= :desc %)  desc
                          (keyword? %) (tag= %)
                          (string?  %) (text= %)
                          (pattern? %) (text-match? %)
                          (vector?  %) (zfxml/seq-test %)
                          (map?     %) (attrs= %))))

(defn xml1->
  [loc & preds]
  (first (apply xml-> loc preds)))