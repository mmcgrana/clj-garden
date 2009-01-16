(System/setProperty "weldsnip.env" (or (first *command-line-args*) "dev"))

(require 'ring.jetty 'weldsnip.app)

(ring.jetty/run {:port 8080} weldsnip.app/app)
