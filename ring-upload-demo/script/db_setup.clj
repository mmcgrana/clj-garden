(require 'cljurl.boot)

(let [env (keyword (or (first *command-line-args*) "dev"))]
  (binding [cljurl.boot/env env] (require 'cljurl.config)))

(require 'clj-jdbc.core 'stash.migrations)

(stash.migrations/create-version cljurl.config/data-source)
