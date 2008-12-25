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

(def xml->       zfxml/xml->)
(def xml1->      zfxml/xml1->)
(def rightmost?  zf/leftmost?)
(def rightmost?  zf/rightmost?)
(def desc        zf/descendants)
(def ansc        zf/ancestors)
(def children    zf/children)
(def attr        zfxml/attr)
(def attr=       zfxml/attr=)
(def text        zfxml/text)

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
      (prn id)
      (= id target-id))))

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

