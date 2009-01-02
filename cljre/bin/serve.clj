(require 'cwsg.handlers.jetty 'cljre.app)

(cwsg.handlers.jetty/run cljre.app/app {:port 8000})
