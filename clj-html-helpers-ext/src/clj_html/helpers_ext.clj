(ns clj-html.helpers-ext
  (:use (clj-html core utils helpers)))

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

(defn password-field-tag
  "Returns an html snippet for a password field."
  [name & [value & [opts]]]
  (html [:input {:type "password" :name name :value value
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
  "Return html for a submit button with the given text."
  [text]
  (html [:input {:type "submit" :name "commit" :value text}]))

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

(defn link-to
  "Returns html for a link with anchor text to the url."
  [text url & [opts]]
  (let [title (get opts :title text)]
    (html [:a {:href url :title title} text])))

(defn delete-button
  "Returns html for a form consisting only of a button that, when clicked,
  will send a delete request to the given path."
  [text url]
  (form-to [:delete url]
    (submit-tag text)))

(def #^{:private true} mime-type-strs
  {:rss  "application/rss+xml"
   :atom "application/rss+xml"})

(defn auto-discovery-link-tag
  "Returns an asset auto discovery tag to make browsers aware of e.g. rss feeds.
  feed-type should be one of :rss or :atom.
  Options: :title, :rel, :href."
  [feed-type & [opts]]
  (html [:link {:rel   (or (get opts :rel)   "alternate")
                :type  (or (get opts :type)  (mime-type-strs feed-type))
                :title (or (get opts :title) (.toUpperCase (name feed-type)))
                :href  (get    opts :href)}]))
