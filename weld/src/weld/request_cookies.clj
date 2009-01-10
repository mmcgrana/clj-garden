(in-ns 'weld.request)

(defvar- cookie-expires-formatter
  (DateTimeFormat/forPattern "'; expires='EEE', 'DD-MMM-YYYY HH:MM:SS' GMT'"))

(defn cookie-parse
  "Returns a non-nested map of cookie values corresponding to the given Cookie
  header string, or nil if the given value is nil."
  [cookie-string]
  (querylike-parse #";\s*" cookie-string))

(defn cookies
  "If only the request is given, returns the map of all cookies for the request.
  If additional args are given, they are treated as keys with which to get-in 
  from the cookies map".
  ([request]
   (cookie-parse ((request :headers) "cookie")))
  ([request & keys] (get-in (cookies request) keys)))

(defn cookie-str
  "Returns a string to be used as Set-Cookie value such that a cookie is set
  according to the given key, value, and options."
  [key value & [opts]]
  (let [domain-str    (if-let [domain  (:domain opts)] (str "; domain=" domain))
        path-str      (if-let [path    (:path opts)]   (str "; path=" path))
        expires-str   (if-let [expires (:expires opts)]
                        (.print cookie-expires-formatter expires))]
    (str (url-escape (name key)) "=" (url-escape value)
         domain-str path-str expires-str)))

(defn- conj-cookie
  "Returns response, augmented with a Set-Cookie header for the given
  cookie-string."
  [response cookie-string]
  (let [headers     (response 1)
        new-headers (assoc headers "Set-Cookie"
                       (conj (get-or headers "Set-Cookie" []) cookie-string))]
    (assoc response 1 new-headers)))

(defn with-cookie
  "Returns response, augmented with a Set-Cookie header according to the
  cookie key, val, and opts."
  ([key val opts response]
   (conj-cookie response (cookie-str key val opts)))
  ([key val response]
   (with-cookie key val nil response)))

(defn less-cookie
  "Returns response, augmented with a Set-Cookie header that will delete
  the keyed cookie on the client."
  [key response]
  (conj-cookie response
    (cookie-str key ""
      (merge {:path nil :domain nil :expires (time/zero)}))))