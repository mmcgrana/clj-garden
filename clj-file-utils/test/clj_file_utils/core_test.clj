(ns clj-file-utils.core-test
  (:use clj-unit.core clj-file-utils.core)
  (:import java.io.File))

(def foo (File. "test/clj_file_utils/assets/foo.txt"))

(deftest "file"
  (assert= foo (file "test/clj_file_utils/assets/foo.txt"))
  (assert= foo (file (file "test/clj_file_utils/assets") "foo.txt"))
  (assert= foo (file "test" "clj_file_utils" "assets" "foo.txt")))

(run-tests 'clj-file-utils.core-test)