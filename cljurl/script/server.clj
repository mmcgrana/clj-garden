(System/setProperty "cljurl.env" (or (first *command-line-args*) "dev"))

(require 'cljurl.config 'cwsg.handlers.jetty 'cljurl.app)

(cwsg.handlers.jetty/run {:port 8080} cljurl.app/app))




