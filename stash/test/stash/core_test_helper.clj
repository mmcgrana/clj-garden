(in-ns 'stash.core-test)

(def +data-source+
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName "stash-test")
    (.setUser         "mmcgrana")
    (.setPassword     "")))

(def +post-map+
  {:data-source +data-source+
   :table-name :posts
   :columns
    [[:title      :string]
     [:body       :string]
     [:view_count :integer]
     [:posted_at  :datetime]
     [:popularity :float]
     [:special    :boolean]]})

(def +post+ (compiled-model +post-map+))