(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(println "WeldBlog")

(require
  '[stash.core :as stash]
  '(weldblog
     [app :as app]
     [config :as config]
     [controllers :as controllers]
     [models :as models]
     [routing :as routing]
     [views :as views]))
