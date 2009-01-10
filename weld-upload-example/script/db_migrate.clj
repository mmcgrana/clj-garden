(System/setProperty "weldup.env" (or (first *command-line-args*) "dev"))

(require '(weldup app migrations) 'clj-jdbc.core 'stash.migrations)

(let [version     (Integer. (second *command-line-args*))
      logger      weldup.app/logger
      data-source weldup.app/data-source
      migrations  weldup.migrations/all]
  (stash.core/with-logger logger
    (clj-jdbc.core/with-connection data-source
      (stash.migrations/migrate migrations version))))
