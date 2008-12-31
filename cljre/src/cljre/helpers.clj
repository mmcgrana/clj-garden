(ns cljre.helpers
  (:use clj-html.core ring.controller))

(defmacro defrouting
  [app-host routes]
  `(do
     (def ~'router (ring.routing/compiled-router ~app-host ~routes))
     (def ~'path-info (partial ring.routing/path-info ~'router))
     (def ~'path      (partial ring.routing/path      ~'router))
     (def ~'url-info  (partial ring.routing/url-info  ~'router))
     (def ~'url       (partial ring.routing/url       ~'router))))

(defn respond-js [body]
  (respond body {"Content-Type" "text/javascript"}))

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
  "Returns an html snippet for a text field"
  [name & [opts]]
  (html [:input {:type "text" :name name
                 :value (get opts :value) :id (get opts :id)}]))

(defn hidden-field-tag
  "Returns html for a hidden field."
  [name value]
  (html [:input {:type "hidden" :name name :value value}]))

(defn submit-tag
  "Return html for a submit button with value as the text."
  [value]
  (html [:input {:type "submit" :name "commit" :value value}]))

(defn form-to
  "Returns html for a form."
  [[method url] body]
  (if (browser-method? method)
    (html
      [:form {:method (method-str method) :action url}
        body])
    (html
      [:form {:method "post" :action url}
        (hidden-field-tag "_method" (method-str method))
        body])))

(defn link-tag
  "Returns html for a link with anchor text to the path."
  [anchor path]
  (html [:a {:href path} anchor]))