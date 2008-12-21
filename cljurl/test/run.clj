(use 'clj-unit.core)
(require 'cljurl.app.models-test 'cljurl.routing-test 'cljurl.utils-test)

(run-tests '(
  cljurl.app.models-test
  cljurl.routing-test
  cljurl.utils-test
))