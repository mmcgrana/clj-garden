(require 'cwsg.core
         'cwsg.middleware.file-content-info
         'cwsg.middleware.string-content-length
         'cwsg.middleware.static
         'cljurl.app)

(import '(java.io File))

(cwsg.core/serve {:port 8000}
  (cwsg.middleware.file-content-info/wrap
    (cwsg.middleware.string-content-length/wrap
      (cwsg.middleware.static/wrap (File. "public")
        cljurl.app/app))))
