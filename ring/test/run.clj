(use 'clj-unit.core)
(require-and-run-tests
  '(ring.builder-test
    ring.endpoints.dump-test
    ring.middleware.file-info-test
    ring.middleware.reloading-test
    ring.middleware.show-exceptions-test
    ring.middleware.static-test
    ring.middleware.www-redirect-test))
