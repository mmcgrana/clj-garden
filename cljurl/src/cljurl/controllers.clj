(ns cljurl.controllers
  (:use
    (ring controller request)
    (cljurl routing utils)
    clj-backtrace.repl)
  (:require
    (cljurl
      [models   :as m]
      [views    :as v]
      [config   :as config])
    [stash.core :as stash]))

(defn log-time
  [time]
  (.info config/logger (str "(" time " msecs) "
                            "cljurl.controllers/index {:id \"foo\"}")))

(defmacro with-time-logging
  "Wraps the body such that the time required to execute the body is logged."
  [& body]
  `(with-realtime [time# (do ~@body)]
     (log-time time#)))

(defmacro with-filters
  "Wrap all action code in a try catch that will either show exception details
  or present an error page to the user, as appropriate."
  [api-action & body]
  `(with-time-logging
     (try
       ~@body
       (catch Exception e#
         (if config/log-exceptions?
           (.error config/logger (pst-str e#)))
         (if config/handle-exceptions?
           (if ~api-action
             (respond-500 (v/internal-error-api) {:content-type "text/javascript"})
             (respond-500 (v/internal-error)))
           (throw e#))))))

(defn not-found
  "Render a not found error page."
  [& [request]]
  (respond-404 (v/not-found)))

(defn not-found-api
  "Render a not found error response for api requests."
  [& [request]]
  (respond-404
    (v/not-found-api)
    {:content-type "text/javascript"}))

(defmacro with-shortening
  "Execute the body with the shortening found or render a not found page if
  no shortening was found."
  [api-action [shortening-sym slug-form] & body]
  `(if-let [~shortening-sym (m/find-shortening ~slug-form)]
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
       (respond
         (v/expand-api shortening)
         {:content-type "text/javascript"}))))
