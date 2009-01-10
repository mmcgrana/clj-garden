(require 'cwsg.handlers.jetty 'ringup.app)

(cwsg.handlers.jetty/run {:port 8080} ringup.app/app))
