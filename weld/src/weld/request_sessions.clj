(in-ns 'weld.request)

(def session-cookie-key :weld-request-session-key)

(defn marshal
  "Returns a the session hash data marshaled into a base64 string."
  [sess]
  (base64-encode (pr-str sess)))

(defn unmarshal
  "Returns the session hash data from the given base64 string."
  [marshaled]
  (read-string (base64-decode marshaled)))

(defn load-session
  "Returns the session hash data contained in the cookies of the env, if such
  data is present, or nil otherwise."
  [env]
  (if-let [marshaled (cookies env session-cookie-key)]
    (unmarshal marshaled)))

(defn dump-session
  "Returns a cookie value that can be set to persist the given session on a
  client."
  [sess]
  (let [marshaled (marshal sess)]
    (if (> (.length marshaled) 4000)
      (throwf "Session too big.")
      (cookie-str session-cookie-key marshaled))))

(defn session
  "Returns session data extracted from the env. If only the env is given as
  an argument, returns the complete session hash. Otherwise uses the addional
  args to get-in the session."
  ([env]
   (if-let [loaded (load-session env)]
     (with-meta loaded
       {:had-flash? (contains? loaded :flash)})))
  ([env arg]
   (get (session env) arg)))

(defn write-session
  "Augment a response to include a cookie that will persist the given session."
  [sess resp]
  (let [unflashed (if (get ^sess :had-flash?)
                    (dissoc sess :flash)
                    sess)]
    (conj-cookie resp (dump-session unflashed) )))

(defn reset-session
  "Send a blank session cookie to the client, thereby resetting the session
  state."
  [resp]
  (write-session {} resp))

(defmacro with-session
  "Helper macro for evaluating a body in the context of a sesion extracted
  from an environment."
  [[bind-sess env-form] & body]
  `(let [~bind-sess (session ~env-form)]
     ~@body))

(defn flash-session
  "Like write-session, but stores an addional 'flash' message that will persist
  only through the next request/response cycle. Note that if you flash a session
  the desired single-cyle durability of the message will only occur if the 
  client is resent the session on the subsequent request."
  [sess message resp]
  (write-session (assoc sess :flash message)
    resp))

(defn flash-env
  "Like (with-session [sess env] (flash-session sess message @body)).
  Use only if the session is not otherwise being written to in this request
  (use flash-session in that case)."
  [env message resp]
  (with-session [sess env]
    (flash-session sess message resp)))

(defmacro with-fading-session
  "In cases in which a session may be flashed an therefore needs to be resent
  to the client to prevent the flash from persisting into subsequent requests,
  you can use this declarative helper macro to both read the session (which
  you will need to read flash values) and to write-session on the generated
  response."
  [[sess-bind env] & body]
  `(with-session [~sess-bind ~env]
     (write-session ~sess-bind (do ~@body))))

(defn flash
  "Reads the flash value from a session"
  [sess]
  (:flash sess))
