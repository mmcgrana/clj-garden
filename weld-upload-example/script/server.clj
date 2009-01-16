(System/setProperty "weldup.env" (or (first *command-line-args*) "dev"))

(require 'ring.jetty 'weldup.app)

(ring.jetty/run {:port 8080} weldup.app/app)

