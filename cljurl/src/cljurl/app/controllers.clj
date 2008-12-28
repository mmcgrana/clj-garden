(ns cljurl.app.controllers
  (:use ring.controller
        ring.request
        cljurl.routing)
  (:require [cljurl.app.models :as m]
            [cljurl.app.views  :as v]
            [cljurl.config     :as config]
            [stash.core        :as stash]))

(defmacro with-filters
  "Wrap all action code in a try catch that will either show exception details
  or present an error page to the user, as appropriate."
  [& body]
  `(try
     ~@body
     (catch Exception e#
       (if (config/handle-exceptions?)
         (internal-error (v/internal-error))
         (throw e#)))))

(defn page-not-found
  "Render a not found error page."
  [& [request]]
  (not-found (v/not-found)))

(defn find-shortening
  "Find the shortening based on a slug."
  [slug]
  (stash/find-one m/+shortening+ {:where [:slug := slug]}))

(defmacro with-shortening
  "Execute the body with the shortening found or render a not found page if
  no shortening was found."
  [[shortening-sym slug-form] & body]
  `(if-let [~shortening-sym (find-shortening ~slug-form)]
     (do ~@body)
     (page-not-found)))

(defn index
  "Render a page listing recent shortenings."
  [request]
  (with-filters
    (let [shortenings (m/find-recent-shortenings 10)]
      (render (v/index shortenings)))))

(defn new
  "Renders a form for creating a new shortening."
  [request]
  (with-filters
    (render (v/new (stash/init m/+shortening+)))))

(defn create
  "Consume a url given by the user, find its shortening, and redirect to the
  shortening show page."
  [request]
  (with-filters
    (let [shortening (stash/create m/+shortening+ (params request :shortening))]
      (if (stash/valid? shortening)
        (redirect (path :show shortening))
        (render (v/new shortening))))))

(defn show
  "Show the known expansion of a url."
  [request]
  (with-filters
    (with-shortening [shortening (params request :slug)]
      (render (v/show shortening)))))

(defn expand
  "Redirect a user from a slug to its url expansion."
  [request]
  (with-filters
    (with-shortening [shortening (params request :slug)]
      (m/hit-shortening shortening (remote-ip request))
      (redirect (:url shortening)))))
