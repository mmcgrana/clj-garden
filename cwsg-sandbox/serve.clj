(require 'cwsg.core
         'cwsg.middleware.show-exceptions
         'cwsg.middleware.file-content-info
         'cwsg.middleware.string-content-length
         'cwsg.middleware.static
         'cwsg.apps.dump)

(import '(java.io File))

(cwsg.core/serve {:port 8000}
  (cwsg.middleware.show-exceptions/wrap
    (cwsg.middleware.file-content-info/wrap
      (cwsg.middleware.string-content-length/wrap
        (cwsg.middleware.static/wrap (File. "public")
          cwsg.apps.dump/app)))))
