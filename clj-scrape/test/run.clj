(use 'clj-http-client.core 'clj-scrape.core)

(defn asset-input-stream [name]
  (let [cl     (clojure.lang.RT/ROOT_CLASSLOADER)
        url    (.getResource cl name)
        path   (.getPath url)]
    (java.io.FileInputStream. path)))

(defn string-input-stream [string]
  (java.io.StringReader. string))

(defn asset-dom [name]
  (dom (asset-input-stream name)))

(defn string-dom [string]
  (dom (string-input-stream string)))

(def ghp (string-dom (get (http-get "http://github.com/topfunky") 2)))


; note: dont print out results of zipper/filters, they print out huge...

; random test
; (def foo-dom (parsed-dom (string-input-stream "<html><body>foo</body></html>")))
; (prn (zfxml/xml1-> foo-dom :body zfxml/text))
; "foo"
; (def topics (zfxml/xml1-> dom zf/descendants :div (zfxml/attr :id "leftcolumn") :div))


; test_set_attr
; test_new_element
; test_scan_text


; test_filter_by_attr
; (def bb-dom (asset-dom "files/boingboing.html"))
; (def youtube-link "http://www.youtube.com/watch?v=TvSNXyNw26g&search=chris%20ware")
; (prn (zfxml/xml1-> bb-dom zf/descendants :a  [(zfxml/attr= :href youtube-link)] (zfxml/attr :href)))
; n - (xml1-> bb-dom desc :a (attr= :href youtube-link) (attr :href))
; youtube-link

; test_filter_contains
; (def basic-dom (asset-dom "files/basic.xhtml"))
; (prn (zfxml/xml1-> basic-dom zf/descendants :title zfxml/text))
; n - (xml1-> basic-dom desc :title text)
; (prn (zfxml/xml1-> basic-dom zf/descendants :title [#(re-match? #"Sample" (zfxml/text %))] zfxml/text))
; n - (xml1-> basic-dom desc :title [(text-match? #"Sample")] text)
; "Sample XHTML"

; test_get_element_by_id / test_get_element_by_tag_name
; (prn (zfxml/xml1-> basic-dom zf/descendants :a (zfxml/attr :id)))
; n - (xml1-> basic-dom desc :a (attr :id))
; "link1"
; (prn (zfxml/xml1-> basic-dom zf/descendants :body zf/descendants (zfxml/attr= :id "link1") (zfxml/attr :id)))
; n - (xml1-> basic-dom desc :body desc (id= "link1") (attr :id))
; "link1"


; test_get_elements_star 
; (def small-dom (parsed-dom (string-input-stream "<div><p id='first'>First</p><p id='second'>Second</p></div>")))
; (prn (map :tag (zfxml/xml-> small-dom zf/descendants zip/node)))
; (:html :body :div :p nil :p nil)

; test_output_basic

; test_scan_basic
; (def basic-dom (asset-dom "files/basic.xhtml"))
; (prn (zfxml/xml1-> basic-dom zf/descendants (zfxml/attr= :id "link1") (zfxml/attr :id)))
; n - (xml1-> basic-dom desc (id= "link1") (attr :id))
; (prn (zfxml/xml1-> basic-dom zf/descendants (zfxml/tag= :p) (zfxml/tag= :a) (zfxml/attr :id)))
; n - (xml1-> basic-dom desc :p :a (attr :id))
; (prn (zfxml/xml1-> basic-dom zf/descendants (class= "ohmy") zf/descendants (zfxml/tag= :a) (zfxml/attr :id)))
; n - (xml1-> basic-dom desc (class= "ohmy") desc :a (attr :id))
; (prn (second (zfxml/xml-> basic-dom zf/descendants (zfxml/tag= :p) zfxml/text)))
; n - (xml1-> basic-dom desc :p text)
; (prn (zfxml/xml1-> basic-dom zf/descendants (zfxml/tag= :p) zip/right zfxml/text))
; (prn (zfxml/xml1-> basic-dom zf/descendants (zfxml/tag= :p) (nth-elem 2) zfxml/text))
; n - (xml1-> basic-dom desc :p (nth-elem 2) text)
; (prn (count (zfxml/xml-> basic-dom zf/descendants (zfxml/tag= :p) (attr? :class))))
; n - (count (xml-> basic-dom desc :p (attr? :class)))
; (prn (zfxml/xml1-> basic-dom zf/descendants (zfxml/tag= :p) (attr-match? :class #"final") (zfxml/attr :class)))
; n - (xml1-> basic-dom desc :p (attr-match? :class #"final") (attr :class))
; (prn (count (zfxml/xml-> basic-dom zf/descendants (zfxml/tag= :p) zf/children (zfxml/tag= :a))))
; n - (count (xml1-> basic-dom desc :p children :a))
; (prn (count (zfxml/xml-> basic-dom zf/descendants (zfxml/tag= :p) (class= "ohmy") zf/children (zfxml/tag= :a))))
; n - (count (xml-> basic-dom desc :p (class= "ohmy") children :a))
; (prn)

; test_positional

; test_pace
; (def pace-dom (asset-dom "files/pace_application.html"))
; (prn (zfxml/xml1-> pace-dom zf/descendants :form (zfxml/attr= :name "frmSect11") (zfxml/attr :method)))
; n - (xml1-> pace-dom desc :form (attr= :name "frmSect111") (attr :method))
; (prn)
