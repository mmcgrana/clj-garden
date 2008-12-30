(in-ns 'stash.core-test)

(deftest-db "transaction"
  (try
    (transaction +post+
      (create +post+ +complete-post-map+)
      (throwf "o noes"))
    (catch Exception e))
  (assert= 0 (count-all +post+)))
