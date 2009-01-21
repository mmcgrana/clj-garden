(System/setProperty "weldblog.env" (or (first *command-line-args*) "dev"))

(require 'ring.jetty 'weldblog.app)

(ring.jetty/run {:port 8080} weldblog.app/app)
