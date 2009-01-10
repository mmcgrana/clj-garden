(require 'ringup.app 'clj-jdbc.core '(stash core migrations))

(clj-jdbc.core/with-connection ringup.app/data-source
  (stash.migrations/create-version))