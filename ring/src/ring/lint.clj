(ns ring.linter
  (:use clojure.set)
  (:import (java.io InputStream)))

(defn lint
  "Asserts that spec applied to val returns logical truth, otherwise raises
  an exception with a message produced by applying format to the message-pattern
  argument and a printing of an invalid val."
  [val spec message]
  (try
    (when-not (spec val)
      (throwf "specified %s, but %s was not" message (pr-str val)))
    (catch Exception e
      (when-not (re-find #"^specified " (.getMessage e))
        (throwf "error occured when checking that %s on %s"
          message (pr-str val))))))

(defn lint-namespacing
  "Asserts that all keys are namespaces other than those included in a 
  specified set of permitted unnamspaced keys"
  [map map-name no-namespace-needed]
  (let [must-namespace (difference (keys map) no-namespace-needed)]
    (doseq [k must-namespace]
      (lint k namespace
        (format "user keys in the %s map must be namespaced" map-name)))))

(defn check-env
  "Validates the env, throwing an exception on violations of the spec"
  [env]
  (lint env map?
    "environment must a Clojure map")

  (lint (:server-port env) integer?
    ":server-port must be an Integer")
  (lint (:server-name env) string?
    ":server-name must be a String")
  (lint (:remote-addr env) string?
    ":remote-addr must be a String")
  (lint (:uri env) #(and (string? %) (.startsWith % "/"))
    ":uri must be a String starting with \"/\"")
  (lint (:query-string env) #(or (nil %)
                                 (and (string? %) (not (re-find #"^\s*$" %))))
    ":query-string must be nil or a non-blank String.)
  (lint (:scheme env) #{"http" "https"}
    ":scheme must be one of \"http\" or \"https\"")
  (lint (:request-method env) #{:get :head :options :put :post :delete}
    ":request-method must be one of :get, :head, :options, :put, :post, or :delete")

  (let [headers (:headers env)]
    (lint headers map?
      ":headers must be a Clojure map")
    (doseq [[hname hval] headers]
      (lint hname string?
         "header names must be Strings")
      (lint hname #(= % (.toLowerCase %))
        "header names must be in lower case")
      (lint vname string?
        "header values must be strings")))

  (lint (:content-type env) #(or (nil? %) (string? %))
    ":content-type must be nil or a String")
  (lint (:content-length env) #(or (nil? %) (integer? %))
    ":content-length must be nil or an Integer")
  (lint (:character-encoding env) #(or (nil? %) (string? %))
    ":character-encoding must be nil or a String")

  (lint (:body env) #(or (nil? %) (instance? InputStream %))
    ":body must be nil or an InputStream")

  (lint-namespacing env "environment"
    #{:server-port :server-name :remote-addr :uri :query-string :scheme
      :request-method :content-type :content-length :character-encoding
      :body}))

(defn check-resp
  "Validates the response, throwing an exception on violations of the spec"
  [resp]
  (lint resp map?
    "response must be a Clojure map")

  (lint (:status resp) #(and (integer? %) (>= % 100))
    ":status must be an Intger greater than or equal to 100")

  (let [headers (:headers resp)]
    (lint headers map?
      ":headers must be a Clojure map")
    (doseq [[hname hval] headers]
      (lint hname string?
        "header names must Strings")
      (lint hval #(or (string? %) (every? string? %))
        "header values must be Strings or colls of Strings")))

  (lint (:body resp) #(or (string? %) (instance? File %)
                          (instance? InputStream %))
    ":body must a String, File, or InputStream")

  (lint-namespacing env "response"
    #{:status :headers :boyd}))

(defn wrap
  "Wrap an app to validate incoming environments and outgoing responses
  according to the Ring spec."
  [app]
  (fn [env]
    (check-env env)
    (let [resp (app env)]
      (check-resp resp)
      resp)))
