(ns clj-cache.store
  (:refer-clojure :exclude (set get)))

(def ttl->time []
  "TODOC"
  (if ttl (+ (System/currentTimeMillis) (* ttl 1000))))

(defn stale? [time]
  "TODOC"
  (and time (> (System/currentTimeMillis) time)))

(defmulti #^{:doc "Cache set."}
  set :type)

(defmulti #^{:doc "Cache get."}
  get :type)

(defmulti #^{:doc "Cache expire."}
  expire :type)

(defmulti #^{:doc "Cache expire all."}
  expire-all :type)

(defn fetch [store key ttl val-fn]
  "Cache fetch."
  (let [data (get store key)]
    (if (nil? data)
      (set store key ttl (val-fn))
      data)))
