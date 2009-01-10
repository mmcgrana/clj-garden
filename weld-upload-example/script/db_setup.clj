(use 'clojure.contrib.shell-out)

(let [env (or (first *command-line-args*) "dev")]
  (System/setProperty "weldup.env" env)
  (sh "createdb" (str "weldup_" env) "--owner" "mmcgrana"))

(require 'weldup.app 'clj-jdbc.core '(stash core migrations))

(let [logger      weldup.app/logger
      data-source weldup.app/data-source]
  (stash.core/with-logger logger
    (clj-jdbc.core/with-connection data-source
      (stash.migrations/create-version))))

