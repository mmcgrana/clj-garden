(in-ns 'stash.core-test)

(defn- upcase-title
  [post]
  [(assoc-by post :title (memfn toUpperCase)) true])

(defn- possible-failing
  [post]
  [post (if (post :special) true false)])

(defn- inc-view_count
  [post]
  [(assoc-by post :view_count inc) true])

(defmodel +post-with-callbacks+
  (assoc +post-map+ :callbacks
    {:after-update [upcase-title possible-failing inc-view_count]}))

(deftest "named-callbacks: returns the associated callbacks"
  (assert= nil
    (named-callbacks (init +post+) :after-update))
  (assert= [upcase-title possible-failing inc-view_count]
    (named-callbacks (init +post-with-callbacks+) :after-update)))

(deftest "run-named-callbacks: returns unmodified instance if are no callbacks"
  (let [post (init +post+)]
    (assert= [post true] (run-named-callbacks post :after-update))))

(deftest "run-named-callbacks: completes callback chain if all callbacks pass"
  (let [post (init +post-with-callbacks+
               {:special true :title "test" :view_count 0})]
    (let [[cb-post passed] (run-named-callbacks post :after-update)]
      (assert-that passed)
      (assert= "TEST" (:title cb-post))
      (assert= 1 (:view_count cb-post)))))

(deftest "run-named-callbacks: halts callback chain when callback return true"
  (let [post (init +post-with-callbacks+
               {:special false :title "test" :view_count 0})]
    (let [[cb-post passed] (run-named-callbacks post :after-update)]
      (assert-not passed)
      (assert= "TEST" (:title cb-post))
      (assert= 0 (:view_count cb-post)))))