(use 'clj-unit.core)
(require
  'cljurl.utils
  '(cljurl.app models-test view-helpers-test controllers-test))

(run-tests '(
 ;cljurl.utils-test
 ;cljurl.app.models-test
  cljurl.app.view-helpers-test
 ;cljurl.app.controllers-test
))