(ns cwsg.middleware.www-redirect
  (:use cwsg.utils))

(defn wrap [app]
  "Wrap an app such that any incoming requests to the www. subdomain are
  301 redirected to the non-www, root domain."
  (fn [env]
    (or
      (if-let [server-name (:server-name env)]
        (if-let [less-www (re-get #"^www\.(.+)" 1 server-name)]
          (let [url (str (:scheme env) "://" less-www (:uri env))]
            [301 {"Location" url}
             (str "You are being <a href=\"" url "\">redirected</a>.")])))
      (app env))))
