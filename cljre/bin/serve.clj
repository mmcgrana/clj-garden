(require 'cwsg.handlers.jetty 'cljre.app)

(let [env (if (= "prod" (first *command-line-args*)) :prod :dev)]
  (cwsg.handlers.jetty/run {:port 8080}
    (cljre.app/build-app env)))
