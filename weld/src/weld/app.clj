(ns weld.app
  (:use weld.request weld.routing))

(defmacro log
  [logger message-form]
  `(when ~logger
     (.info ~logger ~message-form)))

(defn request-log [req]
  (str "request: " (.toUpperCase (name (request-method req))) " "
       (full-uri req)))

(defn routing-log [ns-sym fn-sym]
  (str "routing: " (pr-str ns-sym) "/" (pr-str fn-sym)))

(defn params-log [req]
  (str "params: " (pr-str (params req))))

(defn response-log [resp start]
  (str "response: (" (- (System/currentTimeMillis) start) " msecs) "
       (:status resp) "\n"))

(defn spawn-app
  "Returns an app paramaterized by the given router, as compiled by
  weld.routing/compiled-router."
  [router & [logger]]
  (fn [req]
    (log logger (request-log req))
    (let [start (System/currentTimeMillis)
          req+ (init req)]
      (let [[ns-sym fn-sym a-fn r-params]
              (recognize router (request-method req+) (uri req+))
            req++           (assoc-route-params req+ r-params)]
        (log logger (routing-log ns-sym fn-sym))
        (log logger (params-log req++))
        (let [resp (a-fn req++)]
          (log logger (response-log resp start))
          resp)))))
