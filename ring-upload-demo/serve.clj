(require 'cwsg.handlers.jetty 'updemo.app)

(cwsg.handlers.jetty/run {:port 8080}
  updemo.app/app))
