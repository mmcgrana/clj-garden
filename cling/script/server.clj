(System/setProperty "cling.env" (or (first *command-line-args*) "dev"))

(require 'ring.jetty 'cling.app)

(ring.jetty/run {:port 8080} cling.app/app)
