(set! *warn-on-reflection* true)

(require 'cwsg.core 'cljurl.app)

(cwsg.core/serve {:port 8000} cljurl.app/app)
