(in-ns 'stash.core-test)

(defn- upcase-title
  [post]
  [(update-by post :title (memfn toUpperCase)) true])

(defn- possible-failing
  [post]
  [post (if (post :special) true false)])

(defn- inc-view_count
  [post]
  [(update-by post :view_count inc) true])

(defmodel +post-with-callbacks+
  (assoc +post-map+ :callbacks
    {:after-update [upcase-title possible-failing inc-view_count]}))

(deftest "named-cbs returns the associated callbacks"
  (assert= nil
    (named-cbs (init +post+) :after-update))
  (assert= [upcase-title possible-failing inc-view_count]
    (named-cbs (init +post-with-callbacks+) :after-update)))

(deftest "returns unmodified instance if there are no callbacks"
  (let [post (init +post+)]
    (assert= post (run-named-cbs post :after-update))))

(deftest "completes callback chain if all callbacks return true"
  (let [post (init +post-with-callbacks+
               {:special true :title "test" :view_count 0})]
    (let [cb-post (run-named-cbs post :after-update)]
      (assert= "TEST" (:title cb-post))
      (assert= 1 (:view_count cb-post)))))

(deftest "halts callback chain when callback return true"
  (let [post (init +post-with-callbacks+
               {:special false :title "test" :view_count 0})]
    (let [cb-post (run-named-cbs post :after-update)]
      (assert= "TEST" (:title cb-post))
      (assert= 0 (:view_count cb-post)))))