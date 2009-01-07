(ns clj-cache.stores.memory
  (:refer-clojure :exclude (set get))
  (:use clj-cache.core))

(defn init []
  {:type :memory :data (atom {})})

(defmethod set :memory
  [store key val ttl]
  (let [time (ttl->time ttl)]
    (swap! (get store :data) assoc key [time val])
    val))

(defmethod get :memory
  [store key]
  (let [data (get store :data)]
    (if-let [cached (get @data key)]
      (let [[time val] cached]
        (if (not (stale? time))
          val
          (do
            (swap! data dissoc key)
            nil))))))

(defmethod expire :memory
  [store key]
  (swqp! (get store :data) dissoc key))

(defmethod expire-all :memory
  [store]
  (swqp (get store :data) (constantly {})))