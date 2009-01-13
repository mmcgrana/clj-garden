(ns weldup.app
  (:use
    (weld controller request)
    (clj-html core utils helpers helpers-ext)
    clojure.contrib.str-utils
    clj-jdbc.data-sources
    clj-log.core
    weldup.utils)
  (:require
    (weld [routing :as routing] [app :as app])
    [stash.core :as stash]
    [clj-file-utils.core :as file-utils]
    (ring reload backtrace static file-info)))

;; Config
(def app-host "http://localhost:8080")
(def public-dir  (file-utils/file "public"))
(def uploads-dir (file-utils/file "public/uploads"))
(def statics '("/uploads" "/stylesheets" "/javascripts" "/favicon.ico"))
(def reloadables '(weldup.app weldup.utils))
(def data-source
  (pg-data-source {:database "weldup_dev" :user "mmcgrana" :password ""}))
(def logger (new-logger :out :info))

;; Routing
(routing/defrouting
  app-host
  [['weldup.app/index     :index     :get    "/"]
   ['weldup.app/new       :new       :get    "/new"]
   ['weldup.app/create    :create    :put    "/"]
   ['weldup.app/show      :show      :get    "/:id"]
   ['weldup.app/destroy   :destroy   :delete "/:id"]
   ['weldup.app/not-found :not-found :any    "/:path" {:path ".*"}]])

;; Models
(stash/defmodel +upload+
  {:data-source data-source
   :logger logger
   :table-name :uploads
   :pk-init stash/a-uuid
   :columns
     [[:id            :uuid    {:pk true}]
      [:filename      :string]
      [:content_type  :string]
      [:size          :integer]]
   :accessible-attrs
     [:filename :content_type :size]})

(defn upload-file [upload]
  (file-utils/file uploads-dir (:id upload)))

(defn normalize-filename [filename]
  (re-gsub #"(?i)[^a-z0-9_.]" "_" filename))

(defn create-upload [upload-map]
  (stash/transaction +upload+
    (let [upload (stash/create +upload+
                   {:filename     (normalize-filename (:filename upload-map))
                    :content_type (:content-type upload-map)
                    :size         (:size upload-map)})]
      (file-utils/cp (:tempfile upload-map) (upload-file upload)))))

(defn destroy-upload [upload]
  (stash/destroy upload)
  (file-utils/rm-f (upload-file upload)))

(defmacro with-layout
  [& body]
  `(html
     (doctype :xhtml-transitional)
     [:html {:xmlns "http://www.w3.org/1999/xhtml"}
       [:head
         [:title "ring upload demo"]
         [:style {:type "text/css"} "div.upload { margin-bottom: 1em; }"]]
       [:body ~@body]]))

(defn index-view [uploads]
  (with-layout
    [:p (link-to "new upload" (path :new))]
    [:h2 "Uploads (" (count uploads) ")"]
    (html-for [upload uploads]
      [:div.upload
        (link-to (h (:filename upload)) (path :show upload)) " "
        (delete-button "Delete" (path :destroy upload))])))

(defn new-view []
  (with-layout
    [:p (link-to "back to uploads" (path :index))]
    [:h2 "New Upload"]
    (form-to (path-info :create) {:multipart true}
      (html
        (file-field-tag "upload")
        (submit-tag "Upload")))))

;; Controllers
(defn not-found [& [req]]
  (redirect (path :index)))

(defn index [req]
  (respond (index-view (stash/find-all +upload+))))

(defn new [req]
  (respond (new-view)))

(defn create [req]
  (create-upload (params req :upload))
  (redirect (path :index)))

(defmacro with-upload
  [[binding-sym id-form] & body]
  `(if-let [~binding-sym (stash/get-one +upload+ ~id-form)]
     (do ~@body)
     (not-found)))

(defn show [req]
  (with-upload [upload (params req :id)]
    (send-file (upload-file upload) {:filename (:filename upload)})))

(defn destroy [req]
  (with-upload [upload (params req :id)]
    (destroy-upload upload)
    (redirect (path :index))))

;; Ring app
(def app
  (ring.backtrace/wrap
    (ring.file-info/wrap
      (ring.static/wrap public-dir statics
        (ring.reload/wrap reloadables
          (app/spawn-app router logger))))))
