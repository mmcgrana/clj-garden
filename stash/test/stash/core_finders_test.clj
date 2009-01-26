(in-ns 'stash.core-test)

(deftest "find-sql"
  (assert=
    "SELECT * FROM posts"
    (find-sql +post+))
  (assert=
    "SELECT * FROM posts ORDER BY title DESC"
    (find-sql +post+ {:order [:title :desc]}))
  (assert=
    "SELECT * FROM posts LIMIT 10"
    (find-sql +post+ {:limit 10}))
  (assert=
    "SELECT * FROM posts LIMIT 10 OFFSET 30"
    (find-sql +post+ {:limit 10 :offset 30}))
  (assert=
    "SELECT * FROM posts WHERE title IS NULL"
    (find-sql +post+ {:where "title IS NULL"}))
  (assert=
    "SELECT * FROM posts WHERE (title IS NULL)"
    (find-sql +post+ {:where [:title := nil]}))
  (assert=
    "SELECT * FROM posts WHERE (title IS NOT NULL)"
    (find-sql +post+ {:where [:title :not= nil]}))
  (assert=
    "SELECT * FROM posts WHERE (id = 'foo')"
    (find-sql +post+ {:where [:id := "foo"]}))
  (assert=
    "SELECT * FROM posts WHERE (NOT (id = 'foo'))"
    (find-sql +post+ {:where [:not [:id := "foo"]]}))
  (assert=
    "SELECT * FROM posts WHERE (id IN ('foo', 'bar'))"
    (find-sql +post+ {:where [:id :in ["foo" "bar"]]}))
  (assert=
    "SELECT * FROM posts WHERE ((view_count > 3) AND (view_count < 7))"
    (find-sql +post+ {:where [:and [:view_count :> 3] [:view_count :< 7]]}))
  (assert=
    "SELECT * FROM posts WHERE ((id = 'foo') AND (title = 'bar'))"
    (find-sql +post+ {:where {:id "foo" :title "bar"}}))
  (assert=
    (str "SELECT * FROM posts WHERE (view_count > 3) "
         "ORDER BY title DESC LIMIT 10 OFFSET 30")
    (find-sql +post+
      {:order [:title :desc]
       :limit 10 :offset 30
       :where [:view_count :> 3]})))

(deftest "delte-all-sql"
  (assert=
    "DELETE FROM posts WHERE (id = 'foo')"
    (delete-all-sql +post+ {:where [:id := "foo"]})))

(deftest-db "find-value-by-sql"
  (persist-insert +complete-post+)
  (assert=
    (doto (org.postgresql.util.PGobject.)
       (.setValue (:id +complete-post+)))
    (find-value-by-sql +post+ "select id from posts")))

(deftest-db "find-one-by-sql returns an instance"
  (persist-insert +complete-post+)
  (assert= (:posted_at +complete-post+)
    (:posted_at (find-one-by-sql +post+
      (str "SELECT * FROM posts WHERE (id = '" (:id +complete-post+) "')")))))

(deftest-db "find-all-by-sql"
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (assert=
    (set [+complete-post+ +complete-post-2+])
    (set (find-all-by-sql +post+ "SELECT * FROM posts"))))

(deftest-db "find-one"
  (persist-insert +complete-post+)
  (assert=
    +complete-post+
    (find-one +post+ {:where [:id := (:id +complete-post+)]})))

(deftest-db "find-all"
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (assert=
    (set [+complete-post+ +complete-post-2+])
    (set (find-all +post+)))
  (assert=
    (list +complete-post+)
    (find-all +post+ {:where [:id := (:id +complete-post+)]})))

(deftest-db "get-one: single pk"
  (let [saved (save +complete-post+)]
    (assert-that
      (get-one +post+ (:id +complete-post+)))))

(deftest-db "get-one: multi pk"
  (let [saved (save +simple-hit+)]
    (assert-that
      (get-one +hit+
        [(:path +simple-hit+) (:ip +simple-hit+)]))))

(deftest-db "reload"
  (let [saved    (save +complete-post+)
        reloaded (reload saved)]
    (assert= saved reloaded)
    (assert-not (identical? saved reloaded))))

(deftest-db "delete-all-by-sql"
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (delete-all-by-sql +post+ "DELETE FROM posts")
  (assert= 0 (count-all +post+)))

(deftest-db "exist?: single pk"
  (assert-not (exist? +post+))
  (persist-insert +complete-post+)
  (assert-that (exist? +post+))
  (assert-not  (exist? +post+
                 {:where [:id := (:id (auto-uuid))]}))
  (assert-that (exist? +post+
                 {:where [:id := (:id +complete-post+)]})))

(deftest-db "exist?: multiple pk"
  (assert-not (exist? +schmorg+))
  (persist-insert +simple-schmorg+)
  (assert-that (exist? +schmorg+))
  (assert-not  (exist? +schmorg+
                 {:where [:a_integer := 456]}))
  (assert-that (exist? +schmorg+
                 {:where [:a_integer := (:a_integer +simple-schmorg+)]})))

(deftest-db "count-all"
  (assert= 0 (count-all +post+))
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (assert= 2 (count-all +post+))
  (assert= 1 (count-all +post+ {:where [:id := (:id +complete-post+)]})))

(deftest-db "minimum, maximum"
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (assert= 3 (minimum +post+ :view_count))
  (assert= 7 (maximum +post+ :view_count))
  (assert= 7 (minimum +post+ :view_count
               {:where [:id := (:id +complete-post-2+)]}))
  (assert= 3 (maximum +post+ :view_count
               {:where [:id := (:id +complete-post+)]})))

(deftest-db "delete-all"
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (delete-all +post+)
  (assert= 0 (count-all +post+)))

(deftest-db "delete-all"
  (persist-insert +complete-post+)
  (persist-insert +complete-post-2+)
  (destroy-all +post+)
  (assert= 0 (count-all +post+)))
