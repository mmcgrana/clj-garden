(ns clj-cache.core)

(defn marshal
  "Returns marshaled bytes for the Clojure object."
  [obj]
  (.getBytes (pr-str obj)))

(defn unmarshal
  "Returns a Clojure object for the marshaled bytes."
  [bytes]
  (read-string (String. bytes)))

(defmacro log
  [store action key]
  `(if-let [logger (:logger store)]
     (if ((:test logger) :info)
       ((:log logger) (format (str "cache " action ": " key))))))

(defn write*
  [store key data]
  (log store "write" key)
  ((:write store) key data))

(defn read*
  [store key]
  (if-let [data ((:read store) key)]
    (do
      )
    (do
      (log store "miss"))

(defn delete*
  [store key]
  ((:delete store) key))

(defn flush*
  [store key]
  ((:flush store) key))

(def ttl->time []
  (if ttl (+ (System/currentTimeMillis) (* ttl 1000))))

(defn stale? [time]
  (and time (> (System/currentTimeMillis) time)))

(defn cache-set
  [store key val & [ttl]]
  (let [data  (marshal [(ttl->time ttl) val])]
    (write* store key data)
    val))

(defn cache-get
  [store key]
  (if-let [data (read* store key)]
    (let [[time val] (unmarshal data)]
      (if (not (stale? time))
        val
        (cache-expire store key)))))

(defn cache-delete
  [store key]
  (delete* store key))

(defn cache-flush
  [store key]
  (flush* store key))

(defn cache-fetch [store key ttl val-fn]
  (let [data (cache-get store key)]
    (if (nil? data)
      (cache-set store key ttl (val-fn))
      data))
