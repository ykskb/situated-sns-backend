(ns situated-sns.middleware
  (:require [charmander.core :as charm]
            [clojure.string :as s]
            [environ.core :refer [env]]
            [phrag.db :as phrag-db]
            [ring.middleware.cors :refer [wrap-cors]]))

;;; Util

(defn user-or-throw [req]
  (let [user (:auth-user req)]
    (when (nil? user)
      (throw (ex-info "Not authenticated." {})))
    user))

;;; Auth

(defn- user-by-auth-id [db auth-id]
  (let [user (phrag-db/list-up db :enduser
                               {:where [:= :auth_id auth-id]})]
    (if (> (count user) 0)
      (first user)
      nil)))

(defn- validated-payload [req]
  (let  [token (get-in req [:headers "authorization"])
         validated (charm/validate-token (env :firebase-project-id) token)]
    validated))

(defn- update-user-info [req db]
  (let [payload (validated-payload req)
        user (if (nil? payload)
               nil (user-by-auth-id db (:uid payload)))]
    (-> req
        (assoc :auth-payload payload)
        (assoc :auth-user user)
        (assoc-in [:body-params :variables :authuser_id] (:id user)))))

(defn auth-middleware [db]
  {:name ::auth
   :wrap (fn [handler]
           (fn
             ([req]
                (handler (update-user-info req db)))
             ([req res raise]
              (handler (update-user-info req db) res raise))))})

;;; Conversion from slug to user ID in GraphQL variables

(defn- user-id-from-slug [db slug]
  (let [res (phrag-db/list-up
             db :enduser {:where [:= :slug slug]})]
    (:id (first res))))

(defn- slug-to-user-id [req db]
  (if-let [slug (get-in req [:body-params :variables :slug])]
    (let [query (get-in req [:body-params :query])]
      (if (not (s/includes? query "mutation"))
        (assoc-in req [:body-params :variables :enduser_id]
                  (user-id-from-slug db slug))
        req))
    req))

(defn slug-arg-middleware [db]
  {:name ::slug-to-user-id
   :wrap (fn [handler]
           (fn
             ([req]
              (handler (slug-to-user-id req db)))
             ([req res raise]
              (handler (slug-to-user-id req db) res raise))))})

;;; Reitit CORS middleware

(def reitit-cors-middleware
  #(wrap-cors % :access-control-allow-origin [#".*"]
              :access-control-allow-methods [:get :post]
              :access-control-allow-credentials "true"
              :access-control-allow-headers #{"accept"
                                              "accept-encoding"
                                              "accept-language"
                                              "authorization"
                                              "content-type"
                                              "origin"}))
