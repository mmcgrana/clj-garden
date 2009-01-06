(require 'cwsg.handlers.jetty
         'cwsg.middleware.show-exceptions
         'cwsg.middleware.file-content-info
         'cwsg.middleware.static
         'cwsg.apps.dump)

(import '(java.io File))

(def app
  (cwsg.middleware.show-exceptions/wrap
    (cwsg.middleware.file-content-info/wrap
      (cwsg.middleware.static/wrap (File. "public")
        cwsg.apps.dump/app))))

(cwsg.handlers.jetty/run {:port 8000} app)
