(use 'clj-unit.core)
(require 'cljurl.app)

(require-and-run-tests
  `(cljurl.requests-test cljurl.models-test cljurl.utils-test))

