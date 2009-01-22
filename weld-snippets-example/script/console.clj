(System/setProperty "weldsnip.env" (or (first *command-line-args*) "dev"))

(require
  '[stash.core :as stash]
  '[weldsnip.app :as app])