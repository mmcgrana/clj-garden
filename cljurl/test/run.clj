(use 'clj-unit.core)
(require
  'cljurl.config
  'cljurl.utils
  '(cljurl.app models-test view-helpers-test controllers-test))

(binding [cljurl.config/+env+ :test]
  (run-tests '(
    ;cljurl.utils-test
    ;cljurl.app.models-test
    ;cljurl.app.view-helpers-test
    cljurl.app.controllers-test
  )))