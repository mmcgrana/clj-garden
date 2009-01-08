(in-ns 'stash.migrations-test)

(def +data-source+
  (pg-data-source {:database "stash_migrations_test" :user "mmcgrana" :password ""}))

(defmacro deftest-conn [doc [binding-sym] & body]
  `(deftest ~doc
     (jdbc/with-connection [~binding-sym +data-source+]
       ~@body)))

(def +posts-column-defs+
  [[:id        :uuid   {:pk true}]
   [:slug      :string {:unique true}]
   [:title      :string]
   [:author_id :uuid   {:nullable true}]])

(deftest "create-table-sql"
  (assert= (str "CREATE TABLE posts "
                "(id uuid NOT NULL, slug varchar UNIQUE NOT NULL, "
                "title varchar NOT NULL, author_id uuid, PRIMARY KEY (id))")
    (create-table-sql :posts +posts-column-defs+)))

(deftest "rename-table-sql"
  (assert= "ALTER TABLE blog_posts RENAME TO posts"
    (rename-table-sql :blog_posts :posts)))

(deftest "drop-table-sql"
  (assert= "DROP TABLE posts" (drop-table-sql :posts)))

(deftest "add-column-sql"
  (assert= "ALTER TABLE posts ADD COLUMN body varchar NOT NULL"
    (add-column-sql :posts [:body :string])))

(deftest "rename-column-sql"
  (assert= "ALTER TABLE posts RENAME COLUMN body TO content"
    (rename-column-sql :posts :body :content)))

(deftest "drop-column-sql"
  (assert= "ALTER TABLE posts DROP COLUMN body"
    (drop-column-sql :posts :body)))

(deftest "create-index-sql"
  (assert= "CREATE INDEX posts_by_slug_and_title ON posts (slug, title)"
    (create-index-sql :posts [:slug :title])))

(deftest "drop-index-sql"
  (assert= "DROP INDEX posts_by_slug_and_title"
    (drop-index-sql :posts [:slug :title])))

(deftest-conn "ddl functions"
  [conn]
  (create-table  conn :blog_posts +posts-column-defs+)
  (rename-table  conn :blog_posts :posts)
  (add-column    conn :posts [:body :string])
  (rename-column conn :posts :body :content)
  (drop-column   conn :posts :content)
  (create-index  conn :posts [:slug :title])
  (drop-index    conn :posts [:slug :title])
  (drop-table    conn :posts))
