(in-ns 'stash.core-test)

(deftest "inc-attr"
  (assert= {:foo 2} (inc-attr {:foo 1} :foo)))

(deftest "reload"
  (let [saved    (save +complete-post+)
        reloaded (reload saved)]
    (assert= saved reloaded)
    (assert-not (identical? saved reloaded))))
