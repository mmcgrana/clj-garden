(require 'updemo.app 'updemo.migrations 'clj-jdbc.core 'stash.core 'stash.migrations)

(clj-jdbc.core/with-connection [conn (stash.core/data-source updemo.app/+upload+)]
  (stash.migrations/create-version conn)
  (stash.migrations/migrate conn updemo.migrations/all 1 println))

