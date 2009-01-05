(require 'cwsg.handlers.jetty 'cljre.app)

(cwsg.handlers.jetty/run {:port 8000}
  cljre.app/app)
