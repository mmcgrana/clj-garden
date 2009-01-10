(ns ring.middleware.www-redirect-test
  (:use clj-unit.core ring.middleware.www-redirect))

(def app (wrap (fn [env] :passed)))

(def base-env {:scheme "http" :uri "/foo/bar"})

(deftest "wrap: non-ww host"
  (doseq [host [nil "foo.com" "notwww.foo.com"]]
    (assert= :passed (app (assoc base-env :server-name nil)))))

(deftest "wrap: www host"
  (assert=
    {:status  301
     :headers {"Location" "http://foo.com/foo/bar"}
     :body    "You are being <a href=\"http://foo.com/foo/bar\">redirected</a>."}
    (app (assoc base-env :server-name "www.foo.com"))))

