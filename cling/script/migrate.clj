(System/setProperty "cling.env" (or (first *command-line-args*) "dev"))

(require '(cling config migrations) 'stash.migrations)

(stash.migrations/migrate-with
  cling.migrations/all
  (Integer. (second *command-line-args*))
  cling.config/data-source
  cling.config/logger)