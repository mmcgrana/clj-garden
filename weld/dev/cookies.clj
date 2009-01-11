;; Interface examples
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
  (les-cookie :my_cookie
    (success (v/something {}))))


;; Alternate interface
(defn show "Read-only a cookie" [req]
  (respond (str "your cookie val: " (cookies req :a-cookie-key))))

; no
(defn create "Create a cookie, then read it before returning" [req]
  (with-cookies [cookies req]
    (let [new-cookies (assoc cookies :user_id 23)]
      (println new-cookies)
      [new-cookies (respond "you're logged in")])))

(let [cookies     (cookies env)
      new-val     (dosomething-with cookies)]
  (setting-cookie :user_id new-val (respond (v/index))))


(defn create "Simple cookie stash" [req]
  (setting-cookie :user_id 23 (respond "you're logged in")))
  (setting-cookies {:user_id 23 :foobar :bat})

(defn cookies
  ([env]
   (DO COOKIE READING))
  ([env & args]
   (get-in (cookies env) args)))

(def setting-cookies
  [toset response]
  (DO COOKIE WRITING))

(defn setting-cookie
  [key val response]
  (setting-cookies {key val} response))

(defmacro with-cookies
  [[binding-sym env] & body]
  `(let [~binding-sym (cookies env)
         [new-cookies# response#] (do ~@body)]
     (write-cookies new-cookies# response#)))


; lets not get to ambitious with the middleware stuff, but make it future-proof with the env response


; "Cookie: __utma=100242306.65013152.1228534554.1228534554.1228539624.2; __utmc=100242306; __utmz=100242306.1228539624.2.2.utmccn=(referral)|utmcsr=merb.rubyforge.org|utmcct=/|utmcmd=referral; DokuWiki=bd799c27341199a2135535a8b59668bf"
; 
; {"Set-Cookie"
;   ["name=newvalue; expires=date; path=/; domain=.example.org"
;    "othername=othervlaue; expires=date; path=/; domain=.example.org"]}


(defn time/days-from-nom
  [n])

(defn time/strftime
  [format time])

  cookies
    session
      flash

  def some_action
    cookies[:key]                                          # reading
    cookies[:key] = :val                                   # writing
    cookies[:key] = {:value :val :expires 2.days.from_now} # writing with opts
    cookies.delete[:key]                                   # deleting

    also domain stuff
  end

  cookie lifecycle
    if using cookies, set cookie in response
    borswer sends on next request
    if sent by browser and using, may parse from request
    if cookies asked for, read them
    at end of request, send any new, changed, or deleted cookies


  (defn cookies
    [req]
    (if-let [cookie-string (headers req "set-cookie")]
      (let [cookie-hash (http-utils/cookie-parse cookie-string)]
        ())))



; (defn reading-session
;   [request]
;   (let [user-id (session request :user-id)]
;     (if (and user-id (m/find-user-by-id user-id))
;       (success (v/main))
;       (redirect (path c/login)))))
; 
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



