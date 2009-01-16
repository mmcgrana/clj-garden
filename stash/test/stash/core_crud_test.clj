(in-ns 'stash.core-test)

(deftest "insert-sql: single pk"
  (assert=
    (str "INSERT INTO posts (id, title, view_count, posted_at, special) VALUES ('" (:id +simple-post+) "', 'f''oo', NULL, NULL, NULL)")
    (insert-sql +simple-post+)))

(deftest "insert-sql: multiple pks"
  (assert=
    "INSERT INTO hits (path, ip, count) VALUES ('apath', 'anip', 2)"
    (insert-sql +simple-hit+)))

(deftest "update-sql: single pk"
  (assert=
    (str "UPDATE posts SET title = 'f''oo', view_count = NULL, posted_at = NULL, special = NULL WHERE (id = '" (:id +simple-post+) "')")
    (update-sql +simple-post+)))

(deftest "update-sql: multiple pks"
  (assert=
    "UPDATE hits SET count = 2 WHERE ((path = 'apath') AND (ip = 'anip'))"
    (update-sql +simple-hit+)))

(deftest "delete-sql: single pk"
  (assert= (str "DELETE FROM posts WHERE (id = '" (:id +simple-post+) "')")
    (delete-sql +simple-post+)))

(deftest "delete-sql: multiple pks"
  (assert=
    "DELETE FROM hits WHERE ((path = 'apath') AND (ip = 'anip'))"
    (delete-sql +simple-hit+)))

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
  (assert-that (new? +complete-post+)))

(deftest "init: multiple pks"
  (assert-that (new? +simple-hit+)))

(deftest "init: uuid auto pk"
  (assert-match +uuid-re+ (:id (init +post+))))

(deftest "init: integer auto pk"
  (let [new1 (init +schmorg+)
        new2 (init +schmorg+)]
    (assert= (inc (:pk_integer new1)) (:pk_integer new2))))

(deftest "init: casts attrs"
  (assert= 7
    (:view_count (init +post+ (assoc +complete-post-map+ :view_count "7")))))

(def +post-with-save-callbacks+
  (compiled-model
    (assoc +post-map+ :callbacks
      {:before-validation-on-create
         [(fn [i] [(assoc i :track [:before-v]) true])]
       :after-validation-on-create
         [(fn [i] [(assoc-by i :track conj :after-v) true])]
       :before-create
         [(fn [i] [(assoc-by i :track conj :before-c) true])]
       :after-create
         [(fn [i] [(assoc-by i :track conj :after-c) true])]})))

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

(deftest "update-attrs: accessible attrs"
  (assert= 7
    (:view_count
      (update-attrs (init +post+ +complete-post-map+) {:view_count "7"}))))

(deftest "update-attrs: inaccessible attrs"
  (assert-throws
    #"Attempted to mass-assign keys not declared as accessible-attrs.*\:pk_uuid"
    (update-attrs (init +post+ +complete-post-map+)
      {:pk_uuid "foobar" :pk_integer 5})))

(deftest "update"
  (let [saved   (create +post+ +complete-post-map+)
        updated (update saved {:view_count 7})]
    (assert= 7 (:view_count updated))
    (assert= 7 (:view_count (reload updated)))))

(def +post-with-destroy-cbs+
  (compiled-model
    (assoc +post-map+ :callbacks
      {:before-destroy [(fn [i] [(assoc  i :track [:before])        true])]
       :after-destroy  [(fn [i] [(assoc-by i :track #(conj % :after)) true])]})))

(deftest-db "destroy, deleted?"
  (let [destroyed (destroy (create +post-with-destroy-cbs+ +complete-post-map+))]
    (assert-not  (new? destroyed))
    (assert-that (deleted? destroyed))
    (assert-not  (find-one +post+ {:where [:id := (:id destroyed)]}))
    (assert= [:before :after] (:track destroyed))))
