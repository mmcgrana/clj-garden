(System/setProperty "weldsnip.env" (or (first *command-line-args*) "dev"))

(require 'weldsnip.app 'clj-jdbc.core 'stash.migrations)

(let [version (Integer. (second *command-line-args*))]
  (stash.core/with-logger weldsnip.app/logger
    (clj-jdbc.core/with-connection weldsnip.app/data-source
      (stash.migrations/create-table :snippets
        [[:id         :integer  {:pk true :auto true}]
          [:body       :string]
          [:created_at :datetime]]))))
