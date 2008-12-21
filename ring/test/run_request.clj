(use 'clj-unit.core)
(require 'ring.request-test)

(run-tests '(ring.request-test))

; e.g. ring/controller_asset.txt
; (defn asset-input-stream [name]
;   (let [cl     (.getClassLoader clojure.lang.RT)
;         url    (.getResource name)
;         path   (.getPath url)]
;     (java.io.FileInputStream. path)))
