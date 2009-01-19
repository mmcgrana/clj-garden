(ns clj-cache.memory)

(defn init [& [logger]]
  (let [data (atom {})]
    {:type    :memory
     :logger  logger
     :marshal false
     :data    data
     :read    (fn [key] (get @data key))
     :write   (fn [key data] (swap! data assoc key data))
     :delete  (fn [key] swap! data dissoc key)
     :flush   (fn [] (swap! data (constantly {})))}))
