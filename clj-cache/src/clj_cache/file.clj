(ns clj-cache.memory
  (:use clojure.contrib.str-utils file-utils)
  (:import org.apache.commons.io.IOUtils
           (java.io FileInputStream FileOutputStream)))

(defn- cache-file [dir key]
  (apply file-utils/file dir (re-split #"/" key)))

(defn- maybe-cache-file [dir key]
  (let [cfile (cache-file dir key)]
    (if (.exists cfile)
      cfile)))

(defn- cache-parent-and-file [dir key]
  (let [file (cache-file dir key)]
    [(.getParentFile file) file]))

(defn init [dir & [logger]]
  {:type    :file
   :logger  logger
   :marshal true
   :dir     dir
   :read    (fn [key]
              (if-let [cfile (maybe-cache-file dir key)]
                (with-open [cistream (FileInputSream. cfile)]
                  (IOUtils/toByteArray cistream))))
   :write   (fn [key data]
              (let [[parent file] (cache-parent-and-file dir key)]
                (mkdir-p parent)
                (with-open [fistream (FileInputStream. file)]
                  (IOUtils/copy data file))))
   :delete  (fn [key]
              (if-let [cfile (maybe-cache-file dir key)]
                (file-utils/rm-f cfile)))
   :flush   (fn [] (file-utils/rm-rf dir))})
