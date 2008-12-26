(ns cljurl.app.controllers
  (:use ring.controller
        ring.request
        cljurl.routing)
  (:require [cljurl.app.models :as m]
            [cljurl.app.views  :as v]
            [stash.core        :as stash]))

(defn- find-shortening
  "Find the shortening pased on the slug in the request"
  [request]
  (stash/find-one m/+shortening+ {:where [:slug := (params request :slug)]}))

(defn index
  "Render a page listing recent shortenings."
  [request]
  (let [shortenings (m/find-recent-shortenings 10)]
    (render (v/index shortenings))))

(defmacro with-filters
  [& body]
  `(try
     ~@body
     (catch Exception e#
       (throw (Exception. "foiled"))
       (internal-error (v/internal-error)))))

(defn new
  "Renders a form for creating a new shortening."
  [request]
  (with-filters
    (throw (Exception. "failz"))
    (render (v/new (stash/form-init m/+shortening+)))))

(defn create
  "Consume a url given by the user, find its shortening, and redirect to the
  shortening show page."
  [request]
  (let [shortening (stash/form-create m/+shortening+ (params request :shortening))]
    (if (stash/valid? shortening)
      (redirect (path :show shortening))
      (render (v/new shortening)))))

(defn show
  "Show the known expansion of a url."
  [request]
  (if-let [shortening (find-shortening request)]
    (render (v/show shortening))
    (not-found (v/not-found))))

(defn expand
  "Redirect a user from a slug to its url expansion."
  [request]
  (if-let [shortening (find-shortening request)]
    (redirect (get shortening :url))
    (not-found (v/not-found))))

(defn page-not-found
  "Render a not found error page."
  [request]
  (not-found (v/not-found)))


