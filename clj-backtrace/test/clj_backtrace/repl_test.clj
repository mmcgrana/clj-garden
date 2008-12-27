(ns clj-backtrace.repl-test
  (use clj-unit.core clj-backtrace.repl))

(deftest "trim-redundant-elems"
  (assert= '(d c) (trim-redundant-elems '(d c b a) '(f e b a)))
  (assert= '(c)   (trim-redundant-elems '(c b a)   '(f e b a)))
  (assert= '(d c) (trim-redundant-elems '(d c b a) '(e b a))))
