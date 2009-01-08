(require 'cljurl.boot)

(let [env (keyword (or (first *command-line-args*) "dev"))]
  (binding [cljurl.boot/env env] (require 'cljurl.config)))

(require 'cljurl.migrations 'clj-jdbc.core 'stash.migrations)

(let [version (Integer. (second *command-line-args*))]
  (stash.core/with-logger cljurl.config/logger
    (clj-jdbc.core/with-connection cljurl.config/data-source
      (stash.migrations/migrate cljurl.migrations/all version)))
