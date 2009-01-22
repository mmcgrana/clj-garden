(ns weldsnip.app
  (:use
    (weld routing request controller config)
    (clj-html core utils helpers helpers-ext)
    (clj-jdbc data-sources)
    (clj-log core))
  (:require
    (stash [core :as stash]
           [timestamps :as timestamps])
    (ring reload backtrace static file-info)
    weld.app)
  (:import
    (java.io File)))

;; Config & Routing
(def public  (File. "public"))
(def statics '("/stylesheets" "/javascripts" "/favicon.ico"))

(def logger (new-logger :err :info))
(def data-source (pg-data-source {:database "weldsnip_dev" :user "mmcgrana" :password ""}))

(def router
  (compiled-router
    [['weldsnip.app/ping   :ping   :get  "/ping"]
     ['weldsnip.app/new    :new    :get  "/"]
     ['weldsnip.app/show   :show   :get  "/:id"]
     ['weldsnip.app/create :create :post "/"]
     ['weldsnip.app/miss   :miss   :any  "/:path" {:path ".*"}]]))

(use-config {'weld.routing/*router* router
             'weld.app/*logger*     logger})

;; Model
(stash/defmodel +snippet+
  {:data-source data-source
   :logger logger
   :table-name :snippets
   :columns
     [[:id         :integer  {:pk true :auto true}]
      [:body       :string]
      [:created_at :datetime]]
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

(defhtml new-view []
  (layout "Create a Snippet"
    (form-to (path-info :create)
      (html (text-area-tag "snippet[body]" "" {:rows 20 :cols 73}) [:br]
            (submit-tag "Save")))))

(defhtml show-view [snippet]
  (layout (str "Snippet " (:id snippet))
    [:div [:pre [:code.clojure (:body snippet)]]]
    [:div.date (:created_at snippet)]))

(defhtml miss-view []
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
(def app
  (ring.backtrace/wrap
    (ring.file-info/wrap
      (ring.static/wrap public statics
        weld.app/app))))
