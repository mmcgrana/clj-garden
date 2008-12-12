(ns cwsg.middleware.static
  (:use clojure.contrib.except
        cwsg.util)
  (:import (java.io File)))

(defn- ensure-dir
  "Ensures that the given directory exists."
  [dir]
  (throw-if-not (.exists dir)
    "Directory does not exist: %s" dir))

(defn- forbidden
  []
  [403
   {"Content-Type" "text/html"}
   "<html><body><h1>304 Forbidden</h1></body></html>"])

(defn- success
  [file]
  [200 {} file])

(defn- maybe-file
  "Returns the File corresponding to the given relative path within the given
  dir if it exists, or nil if no such file exists."
  [dir path]
  (let [file (File. dir path)]
    (and (.exists file) (.canRead file) file)))

(defn wrap
  "Returns an app that serves a file out the given directory if one exists that
  corresponse to the request, or delegates to the given app if such a file does 
  not exist."
  [dir app]
  (ensure-dir dir)
  (fn [env]
    (if (#{:get :head} (:request-method env))
      (let [uri (url-decode (:uri env))]
        (if (str-includes? ".." uri)
          (forbidden)
          (let [path (cond
                       (.endsWith "/" uri) (str uri "index.html")
                       (re-matches? #"\.[a-z]+$" uri) uri
                       :else (str uri ".html"))]
            (if-let [file (maybe-file dir path)]
              (success file)
              (app env)))))
      (app env))))