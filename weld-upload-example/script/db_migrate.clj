(require '(ringup app migrations) 'clj-jdbc.core 'stash.migrations)

(let [version (Integer. (first *command-line-args*))]
  (clj-jdbc.core/with-connection ringup.app/data-source
    (stash.migrations/migrate ringup.migrations/all version)))