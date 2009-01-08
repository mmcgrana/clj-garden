(require 'updemo.app 'updemo.migrations 'clj-jdbc.core 'stash.core 'stash.migrations)

(stash.core/with-logger updemo.app/logger
  (clj-jdbc.core/with-connection updemo.app/data-source
    (stash.migrations/create-version)
    (stash.migrations/migrate updemo.migrations/all 1)))

