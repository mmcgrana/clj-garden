(System/setProperty "cljurl.env" (or (first *command-line-args*) "dev"))

(require '(cljurl config migrations) 'clj-jdbc.core 'stash.migrations)

(let [version (Integer. (second *command-line-args*))]
  (stash.core/with-logger cljurl.config/logger
    (clj-jdbc.core/with-connection cljurl.config/data-source
      (stash.migrations/migrate cljurl.migrations/all version))))
