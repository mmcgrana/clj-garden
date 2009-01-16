(in-ns 'stash.core-test)

(def +data-source+
  (pg-data-source {:database "stash_test" :user "mmcgrana" :password ""}))

(defmacro with-clean-db
  [& body]
  `(try
     (delete-all +post+)
     (delete-all +schmorg+)
     (delete-all +hit+)
     ~@body
    (finally
      (delete-all +post+)
      (delete-all +schmorg+)
      (delete-all +hit+))))

(defmacro deftest-db
  [doc & body]
  `(deftest ~doc (with-clean-db ~@body)))


(defmodel +schmorg+
  {:data-source +data-source+
   :table-name  :schmorgs
   :columns
     [[:pk_integer :integer  {:pk true :auto true}]
      [:a_uuid     :uuid     {:nullable true}]
      [:a_integer  :integer  {:nullable true}]
      [:a_boolean  :boolean  {:nullable true}]
      [:a_long     :long     {:nullable true}]
      [:a_float    :float    {:nullable true}]
      [:a_double   :double   {:nullable true}]
      [:a_string   :string   {:nullable true}]
      [:a_datetime :datetime {:nullable true}]]
   :accessible-attrs
     [:uuid :boolean :integer :long :float :double :string :datetime]})

(def +empty-schmorg+
  (init +schmorg+ {}))

(def +simple-schmorg-map+
  {:a_boolean true})

(def +simple-schmorg+
  (init* +schmorg+ +simple-schmorg-map+))

(def +post-map+
  {:data-source +data-source+
   :table-name  :posts
   :columns
    [[:id         :uuid     {:pk true :auto true}]
     [:title      :string]
     [:view_count :integer]
     [:posted_at  :datetime]
     [:special    :boolean]]
   :accessible-attrs
    [:title :view_count :posted_at :special]})

(def +post+
  (compiled-model +post-map+))

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

(defmodel +hit+
  {:data-source +data-source+
   :table-name  :hits
   :columns
     [[:path  :string  {:pk true}]
      [:ip    :string  {:pk true}]
      [:count :integer {:pk false}]]
   :accessible-attrs
     [:path :ip :count]})

(def +simple-hit+
  (init +hit+ {:path "apath" :ip "anip" :count 2}))