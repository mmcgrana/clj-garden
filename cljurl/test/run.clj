(use 'clj-unit.core)
(require
  'cljurl.config
  'cljurl.utils-test
  '(cljurl models-test model-helpers-test view-helpers-test requests-test))

(binding [cljurl.config/+env+ :test]
  (run-tests '(
    cljurl.requests-test
    cljurl.model-helpers-test
    cljurl.models-test
    cljurl.utils-test
    cljurl.view-helpers-test
  )))