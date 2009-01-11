
need to read and write the session every time - closer to core
how to disable??

well, how about it just hapens because we need it to write and then need it to
read, so we would just need to remembe to write after reading
maybe I can deal with that

; we always just dissoc existing keys (assuming people don't write twice)
:id -



; Use only if a session is not being explicitly written
(defn flash-env
  ""
  [env message resp]
  (with-session [sess env]
    (flash-session sess message resp)))






(defn create [env]
  (let [post (stash/create +post+ (params env :post))]
    (if (stash/valid? post)
      (flash-response env :post-create-success
        (redirect (path :post post)))))))

(defn create [env]
  (with-session [sess env]
    (let [post (stash/create +post+ (params env :post))]
      (flash-session sess :post-create-success
        (redirect (path :post post))))))

(defn show [env]
  (with-post [post env]
    (with-session [sess env]
      (write-session sess (v/show post sess)))))

(defn show [env]
  (with-post [post env]
    (with-flashed-session [sess env]
      (v/show post sess))))



(defn v/show [post sess]
  (html
    (if-let [success (flash session :success)]
      (html [:p.success success]))
    [:div#main
      (parital-post post)]))


(defn show [env]
  (v/show (find-post)))

(defn v/show [sess]
  (html
    (if-let [notice (flash session :notice))))
  (with-session [sess env]
    (v/s)
    (flashing [sess {:notice "win"}]
      (write-session (respond (v/index))))))

(defn create [env]
  (stash/create post env)
  (flash! env :notice "success")
  (redirect (path :show post)))

(defn show [env]
  (v/show (session env)))


def flash_action
  flash[:notice] = "win"  # setting
  flash[:noitce]          # reading
  flash.keep              # persisting
  flash.keep(:notice)     # selectively persisting
  flash.now               # setting for this request
end

for now:
  session: all shown above
  flash: setting & reading only







(in-ns 'ring.request)

(def max-bytes 4096)

(def base-options
  {:key         "_session_id"
   :secret       nil
   :domain       nil
   :path         "/"
   :expire_after nil})

ensure :key & :secret
get :key, :secret
@digest
@verifier


(defn load-session [req]
  (let [session-data (cookies req @key)
        data         (unmarshal session-data)]
    data))

put (delay (parse-session env)) in ring.session/data
then in app can read into memory (but that does get us anything, transparen,
  only needed when multiple components need access to session)

; not sure how this would work for multiple session sets, without dumping and
; reparsing everything.
(defn session-assoc
  [key value response]
  )

(defn fly-by-read [env]
  (respond (str "your session val" (session env :key))))

(defn multi-read [env]
  (let [session (session env)])
  (:key1 env)
  (:key2 se))
  
(defn action [req]
  (with-session [session req]
    (respond (if (:awsome session) "yes" "no"))))

(defn action [req]
  (with-session [session req]
    [session (respond (if (:awsome session) "yes" "no"))]))

(defn action [req]
  (with-session [session req]
    [(assoc session :awsome true) (respond "pending")]))

(defn action [req]
  (with-session [session req]
    [(dissoc session :awsome) (respond "pending")]))

(defn action [req]
  (with-session [session req]
    [{} (respond "no more sessions for u")]))

(defn action [req]
  (respond (flash (session req) :read-flash-key))
  (respond (flash req :read-flash-key)))
; session is cached, and this is read only, so latter should be good

(defn action [req]
  (with-session [session req]
    (flash req :read-flash-key)
    [(assoc session :foo :bar) (respond "ok")]))

(defn action [req]
  (with-session [session req]
    (flashing [session :success]
      [session (respond "foo")])))

(defn action [req]
  (flashing-session req :success (respond "foo")))

resseting session

(defn action [env]
  (with-auth [sess env]
    (let [post (stash/get +post+ (params :id))]
      (respond (v/show post sess)))))

(defmacro with-auth
  [[bind env] & body]
  `(let [~sess (session ~env)]
     (if (authorized? sess)
       (do ~@body)
       (redirect (path :login)))))

  (with-session [sess env]
    (logged-in? sess)
    (current-user sess)
    ; how to cache current_user?
    (with-current-user [auth-sess sess]
      (with-location [loc-sess sess]
        (with-redirect-back [[path red-session] loc-sess]
          [red-session (redirect path)])))))

how to deal with multiple repeated reads and then writes and cachig
reparse cookies every time -> fail
cache by req id, but then can't expire
perhaps a weld.request/request-state thing we could use

; return [new-session env] to have the session assoced in,
; return env for read-only on the session
; or maybe just do (session req) for read only

; see how this api plays out for sessions, may be the way to go for cookies
; btw doesn the session in trun depend on the cookie jar

so could set delays on every concievable thing, but that kind of sucks too.

maybe use middleware
  coming up, , middleware sets session in env with
  crap, maybe all actions shoudl be functions of envs, with request.clj
  putting a keys in the env for its won purpouses.
  that makes e.g. the session stuff feasable.
  sick inversion of controll



ok so CookieOverflow is a good example of a case where we could use type
switching on exception handling, though 500 probably wont kill us
; cookie sessions only for now
; enabling and dissabling sessions?
session :off
session :on :only=> [:create :update]


