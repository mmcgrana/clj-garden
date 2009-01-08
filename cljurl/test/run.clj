(require 'cljurl.boot)
(use 'clj-unit.core)
(binding [cljurl.boot/env :test] (require 'cljurl.app))

(require-and-run-tests
  `(cljurl.requests-test cljurl.models-test cljurl.utils-test))

