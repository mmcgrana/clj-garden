(require 'cwsg.handlers.jetty 'cljre.app)

(binding [cljre.app/+env+ (if (= "prod" (first *command-line-args*)) :prod :dev)]
  (cwsg.handlers.jetty/run {:port 8000}
    (cljre.app/build-app)))
