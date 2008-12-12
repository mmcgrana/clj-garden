(ns cljurl.app.controllers
  (:use ring.controller
        ring.request
        cljurl.routing)
  (:require [cljurl.app.views  :as v]
            [cljurl.app.models :as m]))

(defn index
  "Render a page from which the user can enter a url to shorten."
  [request]
  (success (v/index)))

(defn create
  "Consume a url given by the user, find its shortening, and redirect to the
  shortening show page."
  [request]
  (let [url (params request :url)
        shortening (m/create-shortening-by-url url)]
    (if (m/valid-shortening? shortening)
      (redirect (path :show shortening))
      (success (v/index)))))

(defn show
  "Show the known expansion of a url."
  [request]
  (if-let [shortening (m/find-shortening-by-slug (params request :slug))]
    (success (v/show shortening))
    (not-found (v/not-found))))

(defn expand
  "Redirect a user from a slug to its url expansion."
  [request]
  (if-let [shortening (m/find-shortening-by-slug (params request :slug))]
    (redirect (:url (shortening)))
    (not-found (v/not-found))))

(defn page-not-found
  [request]
  (not-found (v/not-found)))


