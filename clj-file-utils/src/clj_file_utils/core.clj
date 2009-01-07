(ns clj-file-utils.core
  (:import java.io.File
           org.apache.commons.io.FileUtils))

(defn file
  "Returns an instance of java.io.File based on the given file name (or 
    acutal file) and parent filenames or files. Note that name args given
    as Strings must not include any path seperators."
  ([name]        (File. name))
  ([parent name] (File. parent name))
  ([p q & names] (reduce file (file p q) names)))

(defn size
  "Returns the size in bytes of the file."
  [#^File file]
  (.length file))

(defn cp
  "Copy a file from one location to another, preserving the file date."
  [#^File from-file #^File to-file]
  (FileUtils/copyFile from-file to-file))
