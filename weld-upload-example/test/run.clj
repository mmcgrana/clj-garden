(System/setProperty "weldup.env" "test")
(use 'clj-unit.core 'weld.utils 'weldup.app)

(binding* config
  (require-and-run-tests 'weldup.app-test))
