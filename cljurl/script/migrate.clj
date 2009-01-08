(require 'cljurl.config 'cljurl.migrations 'clj-jdbc.core 'stash.migrations)

(let [env     (keyword (first *command-line-args*))
      version (Integer. (second *command-line-args*))]
  (cljurl.config/in-env! env)
  (clj-jdbc.core/with-connection [conn (cljurl.config/val :data-source)]
    (stash.migrations/create-version conn)
    (stash.migrations/migrate conn updemo.migrations/all version println)))

