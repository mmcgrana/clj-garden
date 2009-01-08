(System/setProperty "cljurl.env" (or (first *command-line-args*) "dev"))

(require 'cljurl.config 'clj-jdbc.core 'stash.core 'stash.migrations)

(stash.core/with-logger cljurl.config/logger
  (clj-jdbc.core/with-connection cljurl.config/data-source
    (stash.migrations/create-version)))

