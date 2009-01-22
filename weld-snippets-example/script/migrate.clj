(defmigration bootstrap 1
  (create-table :snippets
    [[:id          :integer  {:pk true :auto true}]
      [:body       :string]
      [:created_at :datetime]])
  (drop-table :snippets))

(System/setProperty "weldsnip.env" (or (first *command-line-args*) "dev"))

(require 'weldsnip.app 'stash.migrations)

(stash.migrations/migrate-with
  [bootstrap]
  (Integer. (second *command-line-args*))
  weldsnip.app/data-source
  weldsnip.app/logger)