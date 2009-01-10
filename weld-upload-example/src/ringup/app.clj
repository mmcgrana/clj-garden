(ns ringup.app
  (:use
    (ring controller request)
    (clj-html core helpers helpers-ext)
    clojure.contrib.str-utils
    clj-jdbc.data-sources)
  (:require
    [ring.routing                    :as routing]
    [cwsg.middleware.reloading       :as reloading]
    [cwsg.middleware.show-exceptions :as show-exceptions]
    [cwsg.middleware.file-info       :as file-info]
    [cwsg.middleware.static          :as static]
    [ring.app                        :as app]
    [stash.core                      :as stash]
    [clj-file-utils.core             :as file-utils]))

;; Config
(def app-host "http://localhost:8080")
(def public-dir  (java.io.File. "public"))
(def uploads-dir (java.io.File. "public/uploads"))
(def reloadable-namespace-syms '(ringup.app))
(def data-source
  (pg-data-source {:database "ringup_dev" :user "mmcgrana" :password ""}))

;; Routing
(routing/defrouting
  app-host
  [['ringup.app 'index     :index     :get "/"]
   ['ringup.app 'new       :new       :get "/new"]
   ['ringup.app 'create    :create    :put "/"]
   ['ringup.app 'show      :show      :get "/:id"]
   ['ringup.app 'not-found :not-found :any "/:path" {:path ".*"}]])

;; Models
(stash/defmodel +upload+
  {:data-source data-source
   :table-name :uploads
   :pk-init stash/a-uuid
   :columns
     [[:id            :uuid    {:pk true}]
      [:filename      :string]
      [:content_type  :string]
      [:size          :integer]]})

(defn upload-file [upload]
  (file-utils/file uploads-dir (:id upload)))

(defn normalize-filename [filename]
  (re-gsub #"(?i)[^a-z0-9_.]" "_" filename))

(defn create-upload [upload-map]
  (let [upload (stash/create* +upload+
                 {:filename     (normalize-filename (:filename upload-map))
                  :content_type (:content-type upload-map)
                  :size         (:size upload-map)})]
    (file-utils/cp (:tempfile upload-map) (upload-file upload))))


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
    [:h3 "Uploaded"]
    (domap-str [upload uploads]
      (html [:p [:a {:href (path :show upload)} (h (:filename upload))]]))))

(defn new-view []
  (with-layout
    (form-to (path-info :create) {:multipart true}
      (html
        [:p "Select file:"]
        (file-field-tag "upload")
        (submit-tag "Upload")))))

;; Controllers
(defn not-found [req]
  (redirect (path :index)))

(defn index [req]
  (respond (index-view (stash/find-all +upload+))))

(defn new [req]
  (respond (new-view)))

(defn create [req]
  (create-upload (params req :upload))
  (redirect (path :index)))

(defn show [req]
  (if-let [upload (stash/find-one +upload+ {:where [:id := (params req :id)]})]
    (send-file (upload-file upload) {:filename (:filename upload)})
    (not-found [req])))

;; CWSG app
(def app
  (show-exceptions/wrap
    (file-info/wrap
      (static/wrap public-dir
        (reloading/wrap reloadable-namespace-syms
          (app/spawn-app router))))))
