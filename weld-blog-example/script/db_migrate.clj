(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(require '(weldblog config migrations) 'clj-jdbc.core 'stash.migrations)

(let [version     (Integer. (second *command-line-args*))
      logger      weldblog.config/logger
      data-source weldblog.config/data-source
      migrations  weldblog.migrations/all]
  (stash.core/with-logger logger
    (clj-jdbc.core/with-connection data-source
      (stash.migrations/migrate migrations version))))
