(require 'cwsg.core
         'cwsg.middleware.file-content-info
         'cwsg.middleware.string-content-length
         'cwsg.middleware.static
         'cljurl.app)

(cwsg.core/serve {:port 8000}
  (cwsg.middleware.file-content-info/wrap
    (cwsg.middleware.string-content-length/wrap
      (cwsg.middleware.static/wrap (java.io.File. "public")
        cljurl.app/app))))
