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

; not sure how this would work for multiple session sets, without dumping and
; reparsing everything.
(defn session-assoc
  [key value response]
  )

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

def action
  session[:user_id]       # reading
  session[:user_id] = 2   # writing
  session[:user_id] = nil # deleting
  reset-session           # reset the session
end

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

