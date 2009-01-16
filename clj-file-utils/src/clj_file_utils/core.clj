(ns clj-file-utils.core
  (:import (java.io File IOException)
           org.apache.commons.io.FileUtils)
  (:use clojure.contrib.shell-out))

(defn file
  "Returns an instance of java.io.File based on the given file name (or 
    acutal file) and parent filenames or files. Note that name args given
    as Strings must not include any path seperators."
  ([name]        (File. name))
  ([parent name] (File. parent name))
  ([p q & names] (reduce file (file p q) names)))

(defn exist
  "Returns true if the file exists"
  [#^File file]
  (.exists file))

(defn size
  "Returns the size in bytes of a file."
  [#^File file]
  (.length file))

(defn mv
  "Move a file from one location to another, preserving the file data."
  [#^File from-file #^File to-file]
  (FileUtils/moveFile from-file to-file))

(defn cp
  "Copy a file from one location to another, preserving the file date."
  [#^File from-file #^File to-file]
  (FileUtils/copyFile from-file to-file))

(defn cp-r
  "Copy a directory from one location to another, preseing the file data."
  [#^File from-dir #^File to-dir]
  (FileUtils/copyDirectory from-dir to-dir))

(defn rm
  "Remove a file. Will throw an exception if the file cannot be deleted."
  [#^File file]
  (if-not (.delete file)
    (throw (IOException.))))

(defn rm-f
  "Remove a file, ignoring any errors."
  [#^File file]
  (FileUtils/forceDelete file))

(defn rm-r
  "REmove a directory. The directory must be empty; will throw an exception
  if it is not or if the file cannot be deleted."
  [#^File dir]
  (if-not (.delete dir)
    (throw (IOException.))))

(defn rm-rf
  "Remove a directory, ignoring any errors."
  [#^File dir]
  (FileUtils/forceDelete dir))

(defn touch
  "'touch' as file, as with the Unix command."
  [#^File file]
  (FileUtils/touch file))

(defn mkdir-p
  "Create the directory, including any required but nonexist parents.
  The method does not throw an exception if the complete directory tree
  already exists."
  [#^File file]
  (when-not (.exists file)
    (FileUtils/forceMkdir file)))

(defn chmod
  "'chmod' a file to a mode given as a 4-character string. Only works on
  system with a chmod command."
  [#^File file #^String mode]
  (sh "chmod" mode (.getAbsolutePath file)))
