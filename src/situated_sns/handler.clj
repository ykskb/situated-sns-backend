(ns situated-sns.handler
  (:require [clojure.java.io :as io]
            [environ.core :refer [env]]
            [situated-sns.db :as d]
            [situated-sns.middleware :as mid]))

(defn- file-extension [s]
  (second (re-find #"(\.[a-zA-Z0-9]+)$" s)))

(defn- uuid [] (str (java.util.UUID/randomUUID)))

(defn- save-image [req dir]
  (let [file (get-in req [:parameters :multipart :file])
        ext (file-extension (:filename file))]
    (if (contains? #{".jpg" ".JPG" ".jpeg" ".JPEG" ".png" ".PNG"} ext)
      (let [filename (str (uuid) ext)]
        (io/copy (:tempfile file) (io/file dir filename))
        filename)
      (throw (ex-info "Not supported file type" {})))))

(defn- create-post-image [db filename user]
  (d/create-root {:name filename :created_by (:id user)
                  :url (str (env :service-host) ":" (env :service-port)
                            "/public/post-image/" filename)}
                 db :post_image [:id]))

(defn- update-profile-image [db filename user]
  (d/update! db :enduser {:id (:id user)}
             {:profile_image_url
              (str (env :service-host) ":" (env :service-port)
                   "/public/profile-image/" filename)}))

(defn post-img-handler [db]
  (fn [req]
    (if-let [user (mid/user-or-throw req)]
      (let [filename (save-image req (str (env :resource-dir) "/post-image"))
            pk-map (create-post-image db filename user)]
        {:status 200
         :body pk-map})
      nil)))

(defn profile-img-handler [db]
  (fn [req]
    (if-let [user (mid/user-or-throw req)]
      (let [filename (save-image req (str (env :resource-dir) "/profile-image"))]
        (update-profile-image db filename user)
        {:status 200
         :body {:result true}})
      nil)))

(defn resource-handler []
  (fn [{:keys [path-params] :as _eq}]
    (let [file-path (str (env :resource-dir) "/" (:dir path-params) "/"
                         (:file path-params))]
      {:status 200
       :body (io/input-stream file-path)})))

(defn auth-user-handler [req]
  (if-let [user (mid/user-or-throw req)]
    {:status 200
     :body (dissoc user :auth_id)}
    nil))
