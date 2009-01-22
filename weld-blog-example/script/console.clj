(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(require
  '[stash.core :as stash]
  '[weld.routing :as routing]
  '(weldblog
     [app :as app]
     [config :as config]
     [controllers :as controllers]
     [models :as models]
     [views :as views]))
