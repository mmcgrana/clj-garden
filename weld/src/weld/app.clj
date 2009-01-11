(ns weld.app
  (:use weld.request weld.routing))

(defmacro log
  [logger message-form]
  `(when ~logger
     (.info ~logger ~message-form)))

(defn request-log [env]
  (str "request: " (.toUpperCase (name (request-method env))) " "
       (full-uri env)))

(defn routing-log [ns-sym fn-sym]
  (str "routing: " (pr-str ns-sym) "/" (pr-str fn-sym)))

(defn params-log [env]
  (str "params: " (pr-str (params env))))

(defn response-log [resp start]
  (str "response: (" (- (System/currentTimeMillis) start) " msecs) "
       (:status resp) "\n"))

(defn spawn-app
  "Returns an app paramaterized by the given router, as compiled by
  weld.routing/compiled-router."
  [router & [logger]]
  (fn [env]
    (log logger (request-log env))
    (let [start (System/currentTimeMillis)
          env+ (init env)]
      (let [[ns-sym fn-sym a-fn r-params]
              (recognize router (request-method env+) (uri env+))
            env++           (assoc-route-params env+ r-params)]
        (log logger (routing-log ns-sym fn-sym))
        (log logger (params-log env++))
        (let [resp (a-fn env++)]
          (log logger (response-log resp start))
          resp)))))
