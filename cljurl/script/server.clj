(require 'cljurl.boot)

(let [env (keyword (or (first *command-line-args*) "dev"))]
  (binding [cljurl.boot/env env] (require 'cljurl.config))
  (require 'cwsg.handlers.jetty 'cljurl.app)
  (cwsg.handlers.jetty/run {:port 8080} cljurl.app/app))




