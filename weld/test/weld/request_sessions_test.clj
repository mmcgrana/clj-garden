(in-ns 'weld.request-test)

(deftest "marshal, unmarshal"
  (let [data {:foo "bar"}]
    (assert= data (unmarshal (marshal data)))))

(deftest "dump-session, load-session"
  (let [dumped        (dump-session {:foo "bar"})]
    (assert= "weld-request-session-key=ezpmb28gImJhciJ9" dumped)
    (assert= {:foo "bar"}
      (load-session (env-with {:headers {"cookie" dumped}})))))

(defn response-cookies
  [resp]
  (get-in resp [:headers "Set-Cookie"]))

(defn env-with-cookies
  [cookies]
  (env-with {:headers {"cookie" (str-join "; " cookies)}}))

(defn env-from-response
  [resp]
  (env-with-cookies (response-cookies resp)))

(def blank-response
  {:status :s :headers {} :body :s})

(def cookied-env
  (env-from-response (write-session {:foo "bar"} blank-response)))

(deftest "write-session, session"
  (assert= {:foo "bar"} (session cookied-env)))

(deftest "reset-session"
  (assert= (reset-session blank-response) (write-session {} blank-response)))

(deftest "with-session"
  (let [reg-sess (session cookied-env)]
    (with-session [mac-sess cookied-env]
      (assert= reg-sess mac-sess))))

(deftest "session with flash"
  (let [sess      (session cookied-env)
        auth-sess (assoc sess :session-key :session-value)
        auth-resp (write-session auth-sess blank-response)
        auth-env  (env-from-response auth-resp)]
    (assert= :session-value (session auth-env :session-key))
    (with-session [auth-sess auth-env]
      (let [flash-resp (flash-session auth-sess :flash-message blank-response)
            flash-env  (env-from-response flash-resp)]
        (assert= :session-value (session flash-env :session-key))
        (assert= :flash-message (flash (session flash-env)))
        (let [unflashed-resp (with-fading-session [fading-sess flash-env]
                                blank-response)
              unflashed-env  (env-from-response unflashed-resp)]
          (assert= :session-value (session unflashed-env :session-key))
          (assert-nil (flash (session unflashed-env))))))))

(deftest "flash-env"
  (assert=
    (with-session [sess cookied-env]
      (flash-session sess :flash-message blank-response))
    (flash-env cookied-env :flash-message blank-response)))

; get flash - ok
; come back to corret page - ok
; get redirected or come back later to other flashing page - nothing we can do
; don't come to flashing page now, but come later - nothgin we can do
; somehow get redirected to non-flashing page - bad, would show up later
  ; ** This is the one that we have to avoid
; 