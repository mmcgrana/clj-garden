(ns clj-cache.core)

(defn- log
  [store action key]
  (if-let [logger (:logger store)]
    (if ((:test logger) :info)
      ((:log logger) (str "cache " action ": " key)))))

(defn cache-set
  [store key val & [ttl]]
  (log store "set" key)
  ((:set store) key data))

(defn cache-get
  [store key]
  (let [data ((:get store) key)]
    (log store (if data "get (hit)" "get (miss)") key)
    data))

(defn cache-delete
  [store key]
  (log store "delete" key)
  ((:delete store) key))

(defn cache-flush
  [store key]
  (log store "flush" key)
  ((:flush store) key))

(defn cache-fetch [store key ttl val-fn]
  (let [data (cache-get store key)]
    (if (nil? data)
      (cache-set store key ttl (val-fn))
      data)))
