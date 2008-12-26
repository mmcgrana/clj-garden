(in-ns 'stash.core-test)

(deftest "insert-sql: returns good sql, escaping values"
  (assert=
    (str "INSERT INTO posts "
         "(id, title, view_count, posted_at, special) VALUES "
         "('" (:id simple-post) "', 'f''oo', NULL, NULL, NULL)")
    (insert-sql simple-post)))

(deftest "update-sql: returns good sql"
  (assert=
    (str "UPDATE posts SET title = 'f''oo', view_count = NULL, "
          "posted_at = NULL, special = NULL WHERE id = '" (:id simple-post) "'")
    (update-sql simple-post)))

(deftest "delete-sql: returns good sql"
  (assert= (str "DELETE FROM posts WHERE id = '" (:id simple-post) "'")
    (delete-sql simple-post)))

(deftest-db "persist-insert: inserts a record for the instance, returns as new"
  (with-clean-db
    (let [inserted (persist-insert complete-post)]
      (assert-not (new? inserted))
      (assert= 1 (count-all +post+)))))

(deftest-db "persist-update: updates the record for the instance, returns it"
  (with-clean-db
    (assert= complete-post (persist-update (persist-insert complete-post)))))

(deftest-db "delete: deletes the record for the instance, returns as deleted"
  (let [deleted (delete (persist-insert complete-post))]
    (assert-not   (new? deleted))
    (assert-truth (deleted? deleted))))

(deftest "init: returns instance marked as new"
  (assert-truth (new? complete-post))
  (assert= complete-post-map (dissoc complete-post :id)))

(deftest "init: casts attrs"
  (assert= 7
    (:view_count (init +post+ (assoc complete-post-map :view_count "7")))))

(def +post-with-save-callbacks+
  (compiled-model
    (assoc +post-map+ :callbacks
      {:before-validation-on-create
         [(fn [i] [(assoc i :track [:before-v]) true])]
       :after-validation-on-create
         [(fn [i] [(update i :track conj :after-v) true])]
       :before-create
         [(fn [i] [(update i :track conj :before-c) true])]
       :after-create
         [(fn [i] [(update i :track conj :after-c) true])]})))

(deftest-db "save: new instance"
  (let [saved    (save (init +post-with-save-callbacks+ complete-post-map))]
    (assert-not (new? saved))
    (assert-truth (find-one +post+ {:where [:id := (:id saved)]}))
    (assert= [:before-v :after-v :before-c :after-c] (:track saved))))

(deftest-db "save: non-new instance"
  (save (save (init +post+ complete-post-map))))

(deftest-db "create, new?"
  (let [created (create +post+ complete-post-map)]
    (assert-not (new? created))
    (assert-truth (find-one +post+ {:where [:id := (:id created)]}))))

(deftest-db "create: casts attrs"
  (assert= 7
    (:view_count (create +post+ (assoc complete-post-map :view_count "7")))))

(deftest "update-attrs"
  (assert= 7
    (:view_count
      (update-attrs (init +post+ complete-post-map) {:view_count "7"}))))

(def +post-with-destroy-cbs+
  (compiled-model
    (assoc +post-map+ :callbacks
      {:before-destroy [(fn [i] [(assoc  i :track [:before])        true])]
       :after-destroy  [(fn [i] [(update i :track #(conj % :after)) true])]})))

(deftest-db "destroy, deleted?"
  (let [destroyed (destroy (create +post-with-destroy-cbs+ complete-post-map))]
    (assert-not   (new? destroyed))
    (assert-truth (deleted? destroyed))
    (assert-not   (find-one +post+ {:where [:id := (:id destroyed)]}))
    (assert= [:before :after] (:track destroyed))))
