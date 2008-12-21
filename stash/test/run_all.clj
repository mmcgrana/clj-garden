(use 'clj-unit.core)
(require
  'stash.core-test 'stash.utils-test
  'stash.timestamps-test 'stash.validators-test)

(clj-unit.core/run-tests
  '(stash.core-test stash.utils-test
    stash.timestamps-test stash.validators-test))