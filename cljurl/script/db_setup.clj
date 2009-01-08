(require 'cljurl.boot)

(let [env (keyword (or (first *command-line-args*) "dev"))]
  (binding [cljurl.boot/env env] (require 'cljurl.config)))

(require 'clj-jdbc.core 'stash.migrations)

(clj-jdbc.core/with-connection [conn cljurl.config/data-source]
  (stash.migrations/create-version conn))
