(ns clj-cache.hash
  "Very simple cache implementation based on a Clojure hash-map.")

(defn- stale-time
  [ttl]
  (if ttl (+ (System/currentTimeMillis) (* 1000 ttl))))

(defn- stale?
  [time]
  (if time (> time (System/currentTimeMillis))))

(defn init [& [logger]]
  (let [data (atom {})]
    {:type   :hash
     :logger logger
     :data   data
     :get
       (fn [key]
         (if-let [[val time] (get (deref data) key)]
           (if (stale? val)
             (do (swap! data dissoc key) nil)
             val)))
     :set
       (fn [key ttl]
         (swap! data assoc key [(stale-time ttl) val]))
     :delete
       (fn [key]
         (swap! data dissoc key))
     :flush
       (fn []
         (swap! data (constantly {})))}))
