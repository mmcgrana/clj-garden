(require 'cwsg.handlers.jetty 'cljurl.app)

(cwsg.handlers.jetty/run {:port 8080} (cljurl.app/build-app :dev))
