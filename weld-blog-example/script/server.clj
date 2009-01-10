(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(require 'ring.handlers.jetty 'weldblog.app)

(ring.handlers.jetty/run {:port 8080} weldblog.app/app)
