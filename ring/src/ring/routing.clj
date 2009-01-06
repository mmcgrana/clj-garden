(ns ring.routing
  (:use clj-routing.core ring.utils))

(defn compiled-router [root routes]
  "Returns a router object that can then be used in any of the routing functions
  below. TODO: doc routes format."
  {:symbolic-recognizer
     (compile-recognizer
       (map (fn [[ns-sym fn-sym name method path opts]]
              [[ns-sym fn-sym] method path opts])
            routes))
   :path-info
     (compile-generator
       (map (fn [[ns-sym fn-sym name method path opts]]
              [name method path opts])
            routes))
   :root root})

(defn recognize [router method path]
  "Returns an [action-fn params] tuple based on the router, http method, and 
  path, taking advantage of dynamic resolution."
  [method path]
  (let [[[ns-sym fn-sym] params] ((:symbolic-recognizer router) method path)]
    [(ns-resolve ns-sym fn-sym) params]))

(defn path-info
  "Returns a [method path unused-params] tuple based on the router, action name,
  and params."
  [router name & [params]]
  ((:path-info router) name params))

(defn path
  "Returns a path based on the router, action name, and optional params."
  [router name & [params]]
  (second (path-info router name params)))

(defn absolutize
  "Returns a fully qualifid version of the path based on the root in the 
  router."
  [router path]
  (str (:root router) path))

(defn url-info
  "Returns a [method url unused-params] tuple based on the router, action name, 
  and optional params."
  [router name & [params]]
  (let [path-tuple (path-info router name params)]
    (update path-tuple 1 (fn [path] (absolutize router path)))))

(defn url
  "Returns a full url based on the router, action name, and optional params."
  [router name & [params]]
  (second (url-info router name params)))

(defmacro defrouting
  "Define convenience routing functions based on the root and routes coll, as
  per compiled-router.
  Defines vars router, path-info, path, url-info, and url, where the latter
  4 are such that (path-info :foo) <=> (ring.routing/path-info router :foo)."
  [root routes]
  `(do
     (def ~'router (ring.routing/compiled-router ~root ~routes))
     (def ~'path-info (partial ring.routing/path-info ~'router))
     (def ~'path      (partial ring.routing/path      ~'router))
     (def ~'url-info  (partial ring.routing/url-info  ~'router))
     (def ~'url       (partial ring.routing/url       ~'router))))