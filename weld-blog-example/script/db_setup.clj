(use 'clojure.contrib.shell-out)

(let [env (or (first *command-line-args*) "dev")]
  (System/setProperty "weldblog.env" env)
  (sh "createdb" (str "weldblog_" env) "--owner" "mmcgrana"))

(require 'weldblog.config 'clj-jdbc.core '(stash core migrations))

(let [logger      weldblog.config/logger
      data-source weldblog.config/data-source]
  (stash.core/with-logger logger
    (clj-jdbc.core/with-connection data-source
      (stash.migrations/create-version))))

