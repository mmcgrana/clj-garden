(in-ns 'stash.core-test)

(deftest-db "in-transaction"
  (try
    (in-transaction +post+
      (create +post+ complete-post-map)
      (throwf "o noes"))
    (catch Exception e))
  (assert= 0 (count-all +post+)))
