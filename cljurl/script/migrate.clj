(require 'cljurl.boot)

(let [env     (keyword (or (first *command-line-args*) "dev"))
      version (Integer. (second *command-line-args*))]
  (binding [cljurl.boot/env env] (require 'cljurl.config))
  (require 'cljurl.migrations 'clj-jdbc.core 'stash.migrations)
  (clj-jdbc.core/with-connection [conn cljurl.config/data-source]
    (stash.migrations/create-version conn)
    (stash.migrations/migrate conn updemo.migrations/all version println)))

