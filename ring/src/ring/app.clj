(ns ring.app
  (:use ring.request))

(defn spawn-app
  "Returns an app paramaterized by the given recognizer, which should be a fn
  taking two arguments - a method and uri, and returning an action-fn to call
  and a map of route params derived from the uri."
  [recognizer]
  (fn [env]
    (let [req                      (from-env env)
          [action-fn route-params] (recognizer (request-method req) (uri req))
          request                  (assoc-route-params req route-params)]
      (action-fn request))))