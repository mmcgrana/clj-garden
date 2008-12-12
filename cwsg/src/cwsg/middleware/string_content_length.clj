(ns cwsg.middleware.string-content-length)

(defn wrap
  "Returns an app corresponding to the given one but for which responses with 
  String bodies have corresponding Content-Length headers set."
  [app]
  (fn [env]
    (let [[status headers body :as tuple] (app env)]
      (if (string? body)
        [status (assoc headers "Content-Length" (str (.length body))) body]
        tuple))))