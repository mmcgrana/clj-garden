(in-ns 'stash.migrations-test)

(def +data-source+
  (pg-data-source {:database "stash_migrations_test" :user "mmcgrana" :password ""}))

(defmacro deftest-conn [doc & body]
  `(deftest ~doc
     (jdbc/with-connection +data-source+
       ~@body)))

(def +posts-column-defs+
  [[:id        :uuid   {:pk true}]
   [:slug      :string {:unique true}]
   [:title      :string]
   [:author_id :uuid   {:nullable true}]])

(deftest "create-table-sql: plain pk"
  (assert= "CREATE TABLE posts (id uuid NOT NULL, slug varchar UNIQUE NOT NULL, title varchar NOT NULL, author_id uuid, PRIMARY KEY (id))"
    (create-table-sql :posts +posts-column-defs+)))

(deftest "create-table-sql: auto uuid pk"
  (assert= "CREATE TABLE posts (id uuid NOT NULL, slug varchar UNIQUE NOT NULL, title varchar NOT NULL, author_id uuid, PRIMARY KEY (id))"
    (create-table-sql :posts
      (assoc-in +posts-column-defs+ [0 2] {:pk true :auto :true}))))

(deftest "create-table-sql: auto integer pk"
  (assert= "CREATE TABLE posts (id int4 NOT NULL, slug varchar UNIQUE NOT NULL, title varchar NOT NULL, author_id uuid, PRIMARY KEY (id)); CREATE SEQUENCE posts_id_seq"
    (create-table-sql :posts
      (assoc-in
        (assoc-in +posts-column-defs+
          [0 2] {:pk true :auto true})
        [0 1] :integer))))

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
  (create-table  :blog_posts +posts-column-defs+)
  (rename-table  :blog_posts :posts)
  (add-column    :posts [:body :string])
  (rename-column :posts :body :content)
  (drop-column   :posts :content)
  (create-index  :posts [:slug :title])
  (drop-index    :posts [:slug :title])
  (drop-table    :posts))
