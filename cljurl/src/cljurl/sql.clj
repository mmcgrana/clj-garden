(ns cljurl.sql
  (:use clojure.contrib.def)
  (:require [clj-jdbc.core :as jdbc]
            [cljurl.config :as config]))

(defvar- data-source
  (doto (org.postgresql.ds.PGPoolingDataSource.)
    (.setDatabaseName config/*database-name*)
    (.setUser         config/*database-user*)
    (.setPassword     config/*database-password*)))

(defn select-map [sql & sets]
  (jdbc/with-connection [conn data-source]
    (jdbc/select-map conn sql sets)))

(defn select-value [sql & sets]
  (jdbc/with-connection [conn data-source]
    (jdbc/select-value conn sql sets)))

(defn modify [sql & sets]
  (jdbc/with-connection [conn data-source]
    (jdbc/modify conn sql sets)))