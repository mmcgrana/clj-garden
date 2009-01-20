(System/setProperty "weldup.env" (or (first *command-line-args*) "dev"))

(require '(weldup app migrations) 'stash.migrations)

(stash.migrations/migrate-with
  weldup.migrations/all
  (Integer. (second *command-line-args*))
  weldup.app/data-source
  weldup.app/logger)