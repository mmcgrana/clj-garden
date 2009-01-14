;;; A snippet server implemented with Ring, Weld, and Stash.
;;; Based on the snippet server in 'Programming Clojure'

(ns weldsnip.app
  (:use (weld routing request controller app config)
        (clj-html core utils helpers helpers-ext)
        clj-jdbc.data-sources
        clj-log.core)
  (:require
    (stash [core :as stash] [timestamps :as timestamps])
    (ring jetty reload backtrace static file-info))
  (:import java.io.File))

;; Config & Routing
(def host "http://localhost:8080")
(def public  (File. "public"))
(def statics '("/stylesheets" "/javascripts" "/favicon.ico"))

(def logger (new-logger :err :info))
(def data-source
  (pg-data-source {:database "weldsnip_dev" :user "mmcgrana" :password ""}))

(def router
  (compiled-router
    [['weldsnip.app/ping   :ping   :get  "/ping"]
     ['weldsnip.app/new    :new    :get  "/"  ]
     ['weldsnip.app/show   :show   :get  "/:id"]
     ['weldsnip.app/create :create :post "/"]
     ['weldsnip.app/miss   :miss   :any  "/:path" {:path ".*"}]]))

(use-config {'weld.routing/*router* router
             'weld.routing/*host*   host
             'weld.app/*logger*     logger})

;; Model
(stash/defmodel +snippet+
  {:data-source data-source
   :logger logger
   :table-name :snippets
   :columns
     [[:id         :uuid     {:pk true}]
      [:body       :string]
      [:created_at :datetime]]
   :pk-init stash/a-uuid
   :accessible-attrs
     [:body :created_at]
   :callbacks
     {:before-create [timestamps/timestamp-create]}})

;; View
(defmacro layout [title & body]
  `(html
     [:head
       [:title ~title]
       (include-js "/javascripts/code-highlighter.js" "/javascripts/clojure.js")
       (include-css "/stylesheets/code-highlighter.css")]
     [:body
       [:h2 ~title]
       ~@body]))

(defn new-view []
  (layout "Create a Snippet"
    (form-to (path-info :create)
      (html (text-area-tag "snippet[body]" "" {:rows 20 :cols 73}) [:br]
            (submit-tag "Save")))))

(defn show-view [snippet]
  (layout (str "Snippet " (:id snippet))
    [:div [:pre [:code.clojure (:body snippet)]]]
    [:div.date (:created_at snippet)]))

(defn miss-view []
  (layout "Not Found"
    [:div "Sorry, couldn't find that."]))

;; Controller
(defn ping [req]
  (respond "Pong"))

(defn new [req]
  (respond (new-view)))

(defn show [req]
  (respond (show-view (stash/get-one +snippet+ (params req :id)))))

(defn create [req]
  (let [snippet (stash/create +snippet+ (params req :snippet))]
    (if (stash/valid? snippet)
      (redirect (path :show snippet))
      (respond (new-view)))))

(defn miss [req]
  (respond (miss-view)))

;; Ring App
(def ring-app
  (ring.backtrace/wrap
    (ring.file-info/wrap
      (ring.static/wrap public statics
        app))))

(ring.jetty/run {:port 8080} ring-app)
