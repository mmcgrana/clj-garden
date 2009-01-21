(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(require '(weldblog config migrations) 'stash.migrations)

(stash.migrations/migrate-with
  weldblog.migrations/all
  (Integer. (second *command-line-args*))
  weldblog.config/data-source
  weldblog.config/logger)