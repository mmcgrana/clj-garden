(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(require 'ring.handlers.jetty 'weldup.app)

(ring.handlers.jetty/run {:port 8080} weldup.app/app)

