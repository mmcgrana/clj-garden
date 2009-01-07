(ns clj-html.helpers-ext
  (:use (clj-html core helpers)))

(defn hstr
  "Returns an html-escaped string representation of val. Like (h (str val))."
  [val]
  (h (str val)))

(defn browser-method?
  "Given a keyword an http method, returns true iff the method is implemented by
  browsers."
  [method]
  (or (= method :get) (= method :post)))

(defn method-str
  "Returns the method string corresponding to the given method keyword"
  [method]
  (name method))

(defn text-field-tag
  "Returns an html snippet for a text field.
  Options: :id :class"
  [name & [value & [opts]]]
  (html [:input {:type "text" :name name :value value
                 :id (get opts :id) :class (get opts :class)}]))

(defn text-area-tag
  "Returns and html snippet for a text area.
  Options: :id :class :rows :cols :readonly :spellcheck."
  [name & [value & [opts]]]
  (html [:textarea {:name name :id (get opts :id)
                    :rows (get opts :rows) :cols (get opts :cols)
                    :readonly (get opts :readonly)
                    :spellcheck (get opts :spellcheck)}
          value]))

(defn hidden-field-tag
  "Returns html for a hidden field."
  [name value]
  (html [:input {:type "hidden" :name name :value value}]))

(defn file-field-tag
  "Returns html for a file input field."
  [name]
  (html [:input {:type "file" :name name}]))

(defn submit-tag
  "Return html for a submit button with value as the text."
  [value]
  (html [:input {:type "submit" :name "commit" :value value}]))

(defn form-to
  "Returns html for a form."
  ([target body]
   (form-to target nil body))
  ([[method url] opts body]
   (if (browser-method? method)
     (html
       [:form {:method (method-str method) :action url}
         body])
     (html
       [:form {:method "post" :action url
               :enctype (if (get opts :multipart) "multipart/form-data")}
         (hidden-field-tag "_method" (method-str method))
         body]))))

(defn link-tag
  "Returns html for a link with anchor text to the path."
  [anchor path]
  (html [:a {:href path} anchor]))