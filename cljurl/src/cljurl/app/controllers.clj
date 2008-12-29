(ns cljurl.app.controllers
  (:use ring.controller
        ring.request
        cljurl.routing
        cljurl.app.controller_helpers)
  (:require [cljurl.app.models :as m]
            [cljurl.app.views  :as v]
            [cljurl.config     :as config]
            [stash.core        :as stash]))

(defmacro with-filters
  "Wrap all action code in a try catch that will either show exception details
  or present an error page to the user, as appropriate."
  [api-action & body]
  `(try
     ~@body
     (catch Exception e#
       (if (config/handle-exceptions?)
         (if ~api-action
           (respond-json-500 (v/internal-error-api))
           (respond-500 (v/internal-error)))
         (throw e#)))))

(defn not-found
  "Render a not found error page."
  [& [request]]
  (respond-404 (v/not-found)))

(defn not-found-api
  "Render a not found error response for api requests."
  [& [request]]
  (respond-json-404 (v/not-found-api)))

(defn find-shortening
  "Find the shortening based on a slug."
  [slug]
  (stash/find-one m/+shortening+ {:where [:slug := slug]}))

(defmacro with-shortening
  "Execute the body with the shortening found or render a not found page if
  no shortening was found."
  [api-action [shortening-sym slug-form] & body]
  `(if-let [~shortening-sym (find-shortening ~slug-form)]
     (do ~@body)
     (if ~api-action (not-found-api) (not-found))))

(defn index
  "Render a page listing recent shortenings."
  [request]
  (with-filters false
    (let [shortenings (m/find-recent-shortenings 10)]
      (respond (v/index shortenings)))))

(defn new
  "Renders a form for creating a new shortening."
  [request]
  (with-filters false
    (respond (v/new (stash/init m/+shortening+)))))

(defn create
  "Consume a url given by the user, find its shortening, and redirect to the
  shortening show page."
  [request]
  (with-filters false
    (let [shortening (stash/create m/+shortening+ (params request :shortening))]
      (if (stash/valid? shortening)
        (redirect (path :show shortening))
        (respond (v/new shortening))))))

(defn show
  "Show the known expansion of a url."
  [request]
  (with-filters false
    (with-shortening false [shortening (params request :slug)]
      (respond (v/show shortening)))))

(defn expand
  "Redirect a user from a slug to its url expansion."
  [request]
  (with-filters false
    (with-shortening false [shortening (params request :slug)]
      (m/hit-shortening shortening (remote-ip request))
      (redirect (:url shortening)))))

(defn expand-api
  "Show the url expansion of url"
  [request]
  (with-filters true
    (with-shortening true [shortening (params request :slug)]
       (respond-json (v/expand-api shortening)))))

; - html/json actions
;   - render html/json as appriopriate if no errors (sep acitons or handled)
;   - render usefull error messages for each type (sep actions or handled)
; - api not found page


