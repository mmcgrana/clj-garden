(ns weldup.app
  (:use
    (weld controller request)
    (clj-html core helpers helpers-ext)
    clojure.contrib.str-utils
    clj-jdbc.data-sources
    clj-log4j.core)
  (:require
    (weld
      [routing :as routing]
      [app     :as app])
    (ring.middleware
      [reloading       :as reloading]
      [show-exceptions :as show-exceptions]
      [file-info       :as file-info]
      [static          :as static])
    [stash.core :as stash]
    [clj-file-utils.core :as file-utils]))

;; Config
(def app-host "http://localhost:8080")
(def public-dir  (file-utils/file "public"))
(def uploads-dir (file-utils/file "public/uploads"))
(def reloadable-namespace-syms '(weldup.app))
(def data-source
  (pg-data-source {:database "weldup_dev" :user "mmcgrana" :password ""}))
(def logger (logger4j :err :info))

;; Routing
(routing/defrouting
  app-host
  [['weldup.app 'index     :index     :get    "/"]
   ['weldup.app 'new       :new       :get    "/new"]
   ['weldup.app 'create    :create    :put    "/"]
   ['weldup.app 'show      :show      :get    "/:id"]
   ['weldup.app 'destroy   :destroy   :delete "/:id"]
   ['weldup.app 'not-found :not-found :any    "/:path" {:path ".*"}]])

;; Models
(stash/defmodel +upload+
  {:data-source data-source
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

;; Views
(defmacro with-layout
  [& body]
  `(html
     (doctype :xhtml-transitional)
     [:html {:xmlns "http://www.w3.org/1999/xhtml"}
       [:head
         [:title "ring upload demo"]]
       [:body ~@body]]))

(defn index-view [uploads]
  (with-layout
    [:p [:a {:href (path :new)} "new upload"]]
    [:h3 (if (> (count uploads) 0) "Uploaded" "None Uploaded Yet")]
    (domap-str [upload uploads]
      (html
        [:p [:a {:href (path :show upload)} (h (:filename upload))]]
        (delete-button "Delete" (path :destroy upload))))))

(defn new-view []
  (with-layout
    (form-to (path-info :create) {:multipart true}
      (html
        [:p "Select file:"]
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
  `(if-let [~binding-sym (stash/find-one +upload+ {:where [:id := ~id-form]})]
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
  (show-exceptions/wrap
    (file-info/wrap
      (static/wrap public-dir
        (reloading/wrap reloadable-namespace-syms
          (app/spawn-app router))))))
