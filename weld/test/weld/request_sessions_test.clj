(in-ns 'weld.request-test)

(deftest "marshal, unmarshal"
  (let [data {:foo "bar"}]
    (assert= data (unmarshal (marshal data)))))

(deftest "dump-session, load-session"
  (let [dumped        (dump-session {:foo "bar"})]
    (assert= "weld-request-session-key=ezpmb28gImJhciJ9" dumped)
    (assert= {:foo "bar"}
      (load-session (req-with {:headers {"cookie" dumped}})))))

(defn response-cookies
  [resp]
  (get-in resp [:headers "Set-Cookie"]))

(defn req-with-cookies
  [cookies]
  (req-with {:headers {"cookie" (str-join "; " cookies)}}))

(defn req-from-response
  [resp]
  (req-with-cookies (response-cookies resp)))

(def blank-response
  {:status :s :headers {} :body :s})

(def cookied-req
  (req-from-response (write-session {:foo "bar"} blank-response)))

(deftest "write-session, session"
  (assert= {:foo "bar"} (session cookied-req)))

(deftest "reset-session"
  (assert= (reset-session blank-response) (write-session {} blank-response)))

(deftest "with-session"
  (let [reg-sess (session cookied-req)]
    (with-session [mac-sess cookied-req]
      (assert= reg-sess mac-sess))))

(deftest "session with flash"
  (let [sess      (session cookied-req)
        auth-sess (assoc sess :session-key :session-value)
        auth-resp (write-session auth-sess blank-response)
        auth-req  (req-from-response auth-resp)]
    (assert= :session-value (session auth-req :session-key))
    (with-session [auth-sess auth-req]
      (let [flash-resp (flash-session auth-sess {:success "!"} blank-response)
            flash-request   (req-from-response flash-resp)]
        (assert= :session-value (session flash-request  :session-key))
        (assert= {:success "!"} (flash (session flash-request )))
        (assert= "!" (flash (session flash-request ) :success))
        (let [unflashed-resp (with-fading-session [fading-sess flash-request ]
                                blank-response)
              unflashed-req  (req-from-response unflashed-resp)]
          (assert= :session-value (session unflashed-req :session-key))
          (assert-nil (flash (session unflashed-req))))))))

(deftest "flash-request "
  (assert=
    (with-session [sess cookied-req]
      (flash-session sess :flash-message blank-response))
    (flash-request  cookied-req :flash-message blank-response)))
