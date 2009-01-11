; authorize-request
; record-hit
; hit-cache
; action
; populate-cache
; put namespace and fn name in env

(defn show [env]
  (if (not (authorized? env))
    (do
      (store-location)
      (if (current-user) (access-denied) (login-required)))
    (do
      (record-hit env)
      (if-let [cached (search-cache env)]
        (respond-cached cached)
        (let [db-results (search-db env)]
          (let [response (v/render db-results)]
            (write-cache response)
            response))))))