(require 'cwsg.handlers.jetty 'cljurl.app)

(cwsg.handlers.jetty/run cljurl.app/app {:port 8000})
