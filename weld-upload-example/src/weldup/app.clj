(ns weldup.app
  (:use
    (weld routing request controller config)
    (clj-html core utils helpers helpers-ext)
    clojure.contrib.str-utils
    clj-jdbc.data-sources
    clj-log.core
    clj-file-utils.core
    ring.builder)
  (:require
    [stash.core :as stash]
    weld.app
    (ring reload backtrace static file-info)))

;; Config & Routing
(def env (keyword (System/getProperty "weldup.env")))
(def public  (file "public"))
(def uploads (file "public/uploads"))
(def statics '("/uploads" "/stylesheets" "/javascripts" "/favicon.ico"))
(def reloadables '(weldup.app weldup.utils))
(def logger (if (= env :dev) (new-logger :out :info)))
(def data-source
  (pg-data-source {:database "weldup_dev" :user "mmcgrana" :password ""}))

(def router
  (compiled-router
    [['weldup.app/index     :index     :get    "/"]
     ['weldup.app/new       :new       :get    "/new"]
     ['weldup.app/create    :create    :put    "/"]
     ['weldup.app/show      :show      :get    "/:id"]
     ['weldup.app/destroy   :destroy   :delete "/:id"]
     ['weldup.app/not-found :not-found :any    "/:path" {:path ".*"}]]))

(def config
  {'weld.routing/*router* router
   'weld.app/*logger* logger})

(use-config config)

;; Models
(stash/defmodel +upload+
  {:data-source data-source
   :logger logger
   :table-name :uploads
   :columns
     [[:id            :uuid    {:pk true :auto true}]
      [:filename      :string]
      [:content_type  :string]
      [:size          :integer]]
   :accessible-attrs
     [:filename :content_type :size]})

(defn upload-file [upload]
  (file uploads (:id upload)))

(defn normalize-filename [filename]
  (re-gsub #"(?i)[^a-z0-9_.]" "_" filename))

(defn create-upload [upload-map]
  (stash/transaction +upload+
    (let [upload (stash/create +upload+
                   {:filename     (normalize-filename (:filename upload-map))
                    :content_type (:content-type upload-map)
                    :size         (:size upload-map)})]
      (cp (:tempfile upload-map) (upload-file upload)))))

(defn destroy-upload [upload]
  (stash/transaction +upload+
    (stash/destroy upload)
    (rm-f (upload-file upload))))

(defmacro layout
  [& body]
  `(html
     (doctype :xhtml-transitional)
     [:html {:xmlns "http://www.w3.org/1999/xhtml"}
       [:head
         [:title "Weld Upload Demo"]
         [:style {:type "text/css"} "div.upload { margin-bottom: 1em; }"]]
       [:body ~@body]]))

(defn index-view [uploads]
  (layout
    [:p (link-to "new upload" (path :new))]
    [:h2 "Uploads (" (count uploads) ")"]
    (for-html [upload uploads]
      [:div.upload
        (link-to (h (:filename upload)) (path :show upload)) " "
        (delete-button "Delete" (path :destroy upload))])))

(defn new-view []
  (layout
    [:p (link-to "back to uploads" (path :index))]
    [:h2 "New Upload"]
    (form-to (path-info :create) {:multipart true}
      (html
        (file-field-tag "upload")
        (submit-tag "Upload")))))

;; Controllers
(defn not-found [& [env]]
  (redirect (path :index)))

(defn index [env]
  (respond (index-view (stash/find-all +upload+))))

(defn new [env]
  (respond (new-view)))

(defn create [env]
  (create-upload (params env :upload))
  (redirect (path :index)))

(defmacro with-upload
  [[binding-sym id-form] & body]
  `(if-let [~binding-sym (stash/get-one +upload+ ~id-form)]
     (do ~@body)
     (not-found)))

(defn show [env]
  (with-upload [upload (params env :id)]
    (send-file (upload-file upload) {:filename (:filename upload)})))

(defn destroy [env]
  (with-upload [upload (params env :id)]
    (destroy-upload upload)
    (redirect (path :index))))

;; Ring app
(def dev-only (partial wrap-if (= env :dev)))

(def app
  (dev-only ring.backtrace/wrap
    (ring.file-info/wrap
      (ring.static/wrap public statics
        (dev-only (partial ring.reload/wrap reloadables)
          weld.app/app)))))
