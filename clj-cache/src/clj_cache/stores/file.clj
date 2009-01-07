(ns clj-cache.stores.memory
  (:refer-clojure :exclude (set get))
  (:use clj-cache.core clojure.contrib.str-utils)
  (:require [clj-file-utils :as file-utils])
  (:import org.apache.commons.io.IOUtils
           (java.io InputStream FileInputStream FileOutputStream)))

(defn init [dir]
  {:type :file :dir dir})

(defn- cache-file [dir key]
  (apply file-utils/file dir (re-split #"/" key)))

(defn- cache-parent-and-file [dir key]
  (let [file (cache-file dir key)]
    [(file-utils/dir file) file]))

(defn- #^OutputStream data-dump [data]
  (TODO))

(defn- data-load [#^InputStream istream]
  (TODO))

(defn- f-write [file data]
  (with-open [fostream (FileOutputStream. file)]
    (with-open [distream  (data-dump data)]
      (IOUtils/copy distream fostream))))

(defn- f-read [file]
  (with-open [fistream (FileInputStream. file)]
    (data-load fistream)))

(defmethod set :file
  [store key val ttl]
  (let [[parent (cache-parent-and-file (get store :dir) key)
        time    (ttl->time ttl)]
    (file-utils/mkdir-p parent)
    (f-write file [time val])
    val))

(defmethod get :file
  [store key]
  (let [file (cache-file (get store :dir) key)]
    (if-let [cached (f-read file)]
      (let [[time val] cached]
        (if (not (stale? time))
          val
          (do (file-utils/rm-f file) nil))))))

(defmethod expire :memory
  [store key]
  (file-utils/rm-f (cache-file (get store :dir) key)))

(defmethod expire-all :memory
  [store]
  (file-utils/rm-rm (get store :dir)))
