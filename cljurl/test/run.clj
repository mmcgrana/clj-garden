(System/setProperty "cljurl.env" "test")

(use 'clj-unit.core)

(require-and-run-tests
  `(cljurl.requests-test cljurl.models-test cljurl.utils-test))

