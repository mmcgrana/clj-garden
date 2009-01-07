(in-ns 'stash.core-test)

(deftest "insert-sql: single pk"
  (assert=
    (str "INSERT INTO posts (id, title, view_count, posted_at, special) VALUES ('" (:id +simple-post+) "', 'f''oo', NULL, NULL, NULL)")
    (insert-sql +simple-post+)))

(deftest "insert-sql: multiple pks"
  (assert=
    "INSERT INTO schmorgs (pk_uuid, pk_integer, a_uuid, a_integer, a_boolean, a_long, a_float, a_double, a_string, a_datetime) VALUES ('5260eb5e-3871-42db-ae94-25f1cdff055e', 3, NULL, NULL, 't', NULL, NULL, NULL, NULL, NULL)"
    (insert-sql +simple-schmorg+)))

(deftest "update-sql: single pk"
  (assert=
    (str "UPDATE posts SET title = 'f''oo', view_count = NULL, posted_at = NULL, special = NULL WHERE (id = '" (:id +simple-post+) "')")
    (update-sql +simple-post+)))

(deftest "update-sql: multiple pks"
  (assert=
    "UPDATE schmorgs SET a_uuid = NULL, a_integer = NULL, a_boolean = 't', a_long = NULL, a_float = NULL, a_double = NULL, a_string = NULL, a_datetime = NULL WHERE ((pk_uuid = '5260eb5e-3871-42db-ae94-25f1cdff055e') AND (pk_integer = 3))"
    (update-sql +simple-schmorg+)))

(deftest "delete-sql: single pk"
  (assert= (str "DELETE FROM posts WHERE (id = '" (:id +simple-post+) "')")
    (delete-sql +simple-post+)))

(deftest "delete-sql: multiple pks"
  (assert=
    "DELETE FROM schmorgs WHERE ((pk_uuid = '5260eb5e-3871-42db-ae94-25f1cdff055e') AND (pk_integer = 3))"
    (delete-sql +simple-schmorg+)))

(deftest-db "persist-insert: inserts a record for the instance, returns as new"
  (with-clean-db
    (let [inserted (persist-insert +complete-post+)]
      (assert-not (new? inserted))
      (assert= 1 (count-all +post+)))))

(deftest-db "persist-update: updates the record for the instance, returns it"
  (with-clean-db
    (assert= +complete-post+ (persist-update (persist-insert +complete-post+)))))

(deftest-db "delete: deletes the record for the instance, returns as deleted"
  (let [deleted (delete (persist-insert +complete-post+))]
    (assert-not   (new? deleted))
    (assert-that (deleted? deleted))))

(deftest "init: single pk"
  (assert-that (new? +complete-post+))
  (assert= +complete-post-map+ (dissoc +complete-post+ :id)))

(deftest "init: multiple pks"
  (assert-that (new? +simple-schmorg+))
  (assert= +simple-schmorg-map+ +simple-schmorg+))

(deftest "init: casts attrs"
  (assert= 7
    (:view_count (init +post+ (assoc +complete-post-map+ :view_count "7")))))

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
  (let [saved    (save (init +post-with-save-callbacks+ +complete-post-map+))]
    (assert-not (new? saved))
    (assert-that (find-one +post+ {:where [:id := (:id saved)]}))
    (assert= [:before-v :after-v :before-c :after-c] (:track saved))))

(deftest-db "save: non-new instance"
  (save (save (init +post+ +complete-post-map+))))

(deftest-db "create, new?"
  (let [created (create +post+ +complete-post-map+)]
    (assert-not (new? created))
    (assert-that (find-one +post+ {:where [:id := (:id created)]}))))

(deftest-db "create: casts attrs"
  (assert= 7
    (:view_count (create +post+ (assoc +complete-post-map+ :view_count "7")))))

(deftest "update-attrs"
  (assert= 7
    (:view_count
      (update-attrs (init +post+ +complete-post-map+) {:view_count "7"}))))

(def +post-with-destroy-cbs+
  (compiled-model
    (assoc +post-map+ :callbacks
      {:before-destroy [(fn [i] [(assoc  i :track [:before])        true])]
       :after-destroy  [(fn [i] [(update i :track #(conj % :after)) true])]})))

(deftest-db "destroy, deleted?"
  (let [destroyed (destroy (create +post-with-destroy-cbs+ +complete-post-map+))]
    (assert-not   (new? destroyed))
    (assert-that (deleted? destroyed))
    (assert-not   (find-one +post+ {:where [:id := (:id destroyed)]}))
    (assert= [:before :after] (:track destroyed))))
