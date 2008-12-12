; Note need to adjust wag
;
; "Cookie: __utma=100242306.65013152.1228534554.1228534554.1228539624.2; __utmc=100242306; __utmz=100242306.1228539624.2.2.utmccn=(referral)|utmcsr=merb.rubyforge.org|utmcct=/|utmcmd=referral; DokuWiki=bd799c27341199a2135535a8b59668bf"
; 
; {"Set-Cookie"
;   ["name=newvalue; expires=date; path=/; domain=.example.org"
;    "othername=othervlaue; expires=date; path=/; domain=.example.org"]}

(defn create
  "Create a cookie"
  (let [some-val (params request :some-val)]
    (with-cookie :my_cookie "my value" {:expires (time/days-from-now 3)}
      (success (v/something {})))))

(defn show
  "Show something based on a cookie."
  [request]
  (let [id (cookies request :search-id)]
    (if-let [search (m/find-search-by-id id)]
      (success (v/show search))
      (not-found (v/not-found)))))

(defn destroy
  "Remove a cookie"
  [request]
  (less-cookie :my_cookie
    (success (v/something {}))))

(defn reading-session
  [request]
  (let [user-id (session request :user-id)]
    (if (and user-id (m/find-user-by-id user-id))
      (success (v/main))
      (redirect (path c/login)))))

; setup cookie session based on request
; reques comes in with cookie
; get cookie valued by session-id-key
; have session-secret-key

(defn reading-session
  [request]
  (let [ses (session request)]
    (success (v/show) {:user-id (get-session ses)})))

(defn writing-session
  [request]
  (let [session (session request)
        new-sesson (assoc-session session :user-id "someid")]
    (success (v/main) {:session new-session})))

(defn generate-cookie)

(defvar- +cookie-expiry-format+ "%a, %d-%b-%Y %H:%M:%S GMT"
  "strftime formatter for Set-Cookie expiry header")

(defn- cookie-string
  [name value options]
  (let [^#StringBilder builder (StringBuilder.)
        secure        (:secure options)
        expires       (:expires options)
        other-options (dissoc options :secure :expires)]
    (.append builder (str (the-str name) "=" (url-escape value) ";"))
    (doseq [[op-name op-val] other-options]
      (.append (str (the-str op-name) "=" op-val ";"))
    (if expires
      (.append builder
        "expires=" (time/strftime +cookie-expiry-format+ expires) ";"))
    (if secure
      (.append builder "secure"))
    (str builder)))

(defn send-cookie
  ([name value response]
   (with-cookie name value {} response))
  ([name value options response]
   (let [headers         (nth response 1)
         cookied-headers (assoc headers "set-cookie"
                           (conj
                             (or (get headers "set-cookie") [])
                             (cookie-string name value options)))]
     (assoc response 1 cookied-headers))))

(defn delete-cookie
  [name response]
  (with-cookie name "" {:expires (time/days-from-now -1)}))

(defn cookies
  "If only the request is given, returns the map of all cookies for the request.
  If additional args are given, they are treated as keys with which to get-in 
  from the cookies map".
  ([request]
   (cookie-parse ((request :headers) "cookie")))
  ([request & keys] (get-in (cookies request) keys)))

(defn time/days-from-nom
  [n])

(defn time/strftime
  [format time])



