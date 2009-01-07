(ns updemo.file-utils
  (:import java.io.File org.apache.commons.io.FileUtils))

; Compojure
(defn file
  "Returns an instance of java.io.File."
  ([name]          (new File name))
  ([parent name]   (new File parent name))
  ([p q & parents] (reduce file (file p q) parents)))


(defn cp
  "Copy a file from one location to another, preserving the file date."
  [#^File from-file #^File to-file]
  (FileUtils/copyFile from-file to-file))

; touch
; mkdir
; mkdir-p
; rm
; rm-r
; cp-r
; mv
; size
; chmod
