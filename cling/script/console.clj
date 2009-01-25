(System/setProperty "cling.env" (or (first *command-line-args*) "dev"))

(require
  '(stash [core :as stash])
  '(weld  [routing :as routing])
  '(cling [app :as app]
          [config :as config]
          [controllers :as controllers]
          [models :as models]
          [views :as views]
          [diff  :as diff]
          [markup :as markup]))
