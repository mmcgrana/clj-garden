(require 'cwsg.handlers.jetty 'cljurl.app)

(cwsg.handlers.jetty/run {:port 8000} cljurl.app/app)
