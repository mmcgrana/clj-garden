(System/setProperty "weldup.env" (or (first *command-line-args*) "dev"))

(require '(weldup config migrations) 'clj-jdbc.core 'stash.migrations)

(let [version     (Integer. (second *command-line-args*))
      logger      weldup.config/logger
      data-source weldup.config/data-source
      migrations  weldup.migrations/all]
  (stash.core/with-logger logger
    (clj-jdbc.core/with-connection data-source
      (stash.migrations/migrate migrations version))))
