(in-ns 'stash.core-test)

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "stash-test")
    (.setUser         "mmcgrana")
    (.setPassword     "")))


(defmacro with-clean-db
  [& body]
  `(try
    (delete-all +post+)
    (delete-all +schmorg+)
     ~@body
    (finally
      (delete-all +post+)
      (delete-all +schmorg+))))

(defmacro deftest-db
  [doc & body]
  `(deftest ~doc (with-clean-db ~@body)))


(defmodel +schmorg+
  {:data-source +data-source+
   :table-name :schmorgs
   :columns
    [[:uuid     :uuid]
     [:boolean  :boolean]
     [:integer  :integer]
     [:long     :long]
     [:float    :float]
     [:double   :double]
     [:string   :string]
     [:datetime :datetime]]})

(def empty-schmorg
  (init +schmorg+ {}))


(def +post-map+
  {:data-source +data-source+
   :table-name :posts
   :columns
    [[:title      :string]
     [:view_count :integer]
     [:posted_at  :datetime]
     [:special    :boolean]]})

(def +post+ (compiled-model +post-map+))

(def simple-post
  (init +post+ {:title "f'oo"}))

(def complete-post-map
  {:title "foo" :view_count 3 :posted_at (now) :special true})

(def complete-post-2-map
  {:title "biz" :view_count 7 :posted_at (now) :special false})

(def complete-post
  (init +post+ complete-post-map))

(def complete-post-2
  (init +post+ complete-post-2-map))