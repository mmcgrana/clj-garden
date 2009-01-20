(System/setProperty "weldup.env" "test")

(use 'clj-unit.core)

(require-and-run-tests 'weldup.app-test)
