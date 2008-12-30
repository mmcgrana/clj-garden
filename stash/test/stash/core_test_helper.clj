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
   :table-name  :schmorgs
   :pks         [:a_uuid :a_integer]
   :columns
     [[:a_uuid     :uuid]
      [:a_integer  :integer]
      [:a_boolean  :boolean]
      [:a_long     :long]
      [:a_float    :float]
      [:a_double   :double]
      [:a_string   :string]
      [:a_datetime :datetime]]
   :accessible-attrs
     [:uuid :boolean :integer :long :float :double :string :datetime]})

(def +empty-schmorg+
  (init +schmorg+ {}))

(def +simple-schmorg-map+
  {:a_uuid "5260eb5e-3871-42db-ae94-25f1cdff055e"
   :a_integer 3 :a_boolean true})

(def +simple-schmorg+
  (init* +schmorg+ +simple-schmorg-map+))

(def +post-map+
  {:data-source +data-source+
   :table-name  :posts
   :pk          :id
   :pk-init     a-uuid
   :columns
    [[:id         :uuid]
     [:title      :string]
     [:view_count :integer]
     [:posted_at  :datetime]
     [:special    :boolean]]
   :accessible-attrs
    [:title :view_count :posted_at :special]})

(def +post+ (compiled-model +post-map+))

(def +simple-post+
  (init +post+ {:title "f'oo"}))

(def +complete-post-map+
  {:title "foo" :view_count 3 :posted_at (now) :special true})

(def +complete-post-2-map+
  {:title "biz" :view_count 7 :posted_at (now) :special false})

(def +complete-post+
  (init +post+ +complete-post-map+))

(def +complete-post-2+
  (init +post+ +complete-post-2-map+))