(ns situated-sns.system
  (:require [integrant.core :as ig]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [phrag.route :as phrag-route]
            [ring.adapter.jetty :as jetty]
            [hikari-cp.core :as hkr]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.spec]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.middleware.multipart :as multipart]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.dev :as dev]
            [ring.middleware.params :as params]
            [ring-graphql-ui.core :as gql]
            [situated-sns.middleware :as mid]
            [situated-sns.signal :as sig]
            [situated-sns.handler :as hd]
            [muuntaja.core :as m]))

(defmethod ig/init-key ::custom-routes [_ db]
  [["/auth-user" {:get {:handler hd/auth-user-handler}
                  :middleware [mid/reitit-cors-middleware]}]
   ["/post-image"
    {:post {:handler (hd/post-img-handler db)
            :parameters {:multipart {:file multipart/temp-file-part}}}
     :middleware [mid/reitit-cors-middleware]}]
   ["/profile-image"
    {:post {:handler (hd/profile-img-handler db)
            :parameters {:multipart {:file multipart/temp-file-part}}}
     :middleware [mid/reitit-cors-middleware]}]
   ["/public/:dir/:file"
    {:get {:handler (hd/resource-handler)}}]])

;;; App

(defmethod ig/init-key ::app [_ {:keys [db gql-route custom-routes]}]
  (ring/ring-handler
   (ring/router
    [custom-routes
     gql-route]
    {:data {:coercion reitit.coercion.spec/coercion
            :muuntaja m/instance
            :middleware [parameters/parameters-middleware
                         ;; params/wrap-params
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         coercion/coerce-response-middleware
                         coercion/coerce-request-middleware
                         multipart/multipart-middleware
                         (mid/auth-middleware db)
                         (mid/slug-arg-middleware db)]}})
   (ring/routes
    (gql/graphiql {:endpoint "/graphql"})
    (ring/create-default-handler))))

(defmethod ig/init-key :database.sql/connection [_ db-spec]
  {:connection (jdbc/get-connection db-spec)})

(defmethod ig/init-key :database.sql/conn-pool [_ opts]
  (let [data-src (delay (hkr/make-datasource opts))]
    {:datasource @data-src}))

(defmethod ig/init-key ::server [_ {:keys [app options]}]
  (jetty/run-jetty app options))

(defmethod ig/halt-key! ::server [_ server]
  (.stop server))

(def config {:database.sql/connection
             ;;{:connection-uri "jdbc:sqlite:db/dev.sqlite"}
             {:dbtype (env :db-type)
              :dbname (env :db-name)
              :host (env :db-host)
              :port (env :db-port)
              :user (env :db-user)
              :password (env :db-password)
              :currentSchema (env :db-current-schema)
              :stringtype "unspecified"}
             ::custom-routes (ig/ref :database.sql/connection)

             :database.sql/conn-pool
             {:adapter (env :db-type)
              :username (env :db-user)
              :password (env :db-password)
              :database-name (env :db-name)
              :server-name (env :db-host)
              :port-number (env :db-port)
              :current-schema (env :db-current-schema)
              :string-type "unspecified"}
             :phrag.route/reitit
             {:db (ig/ref :database.sql/connection)
              ;; :signals sig/signals
              ;; :default-limit 100
              :middleware [mid/reitit-cors-middleware]}
             ::app {:gql-route (ig/ref :phrag.route/reitit)
                    :custom-routes (ig/ref ::custom-routes)
                    :db (ig/ref :database.sql/conn-pool)}
             ::server {:app (ig/ref ::app)
                       :options {:port (read-string (env :service-port "3000"))
                                 :join? false}}})

(defn start []
  (ig/init config))
