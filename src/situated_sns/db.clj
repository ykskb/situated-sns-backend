(ns situated-sns.db
  (:require [honey.sql.helpers :as h]
            [honey.sql :as sql]
            [clojure.java.jdbc :as jdbc]))

(defn list-up
  "Executes a select statement from a table with provided parameters."
  [db-spec table params]
  (let [whr (:where params)
        selects (:select params [:*])
        sorts (:sort params)
        q (cond-> (apply h/select selects)
            true (h/from table)
            (:limit params) (h/limit (:limit params))
            (:offset params) (h/offset (:offset params)))
        q (if (not-empty whr) (apply h/where q whr) q)
        q (if (not-empty sorts) (apply h/order-by q sorts) q)]
    (->> (sql/format q)
         (jdbc/query db-spec))))

(defn update!
  "Executes update statement with primary key map and parameter map."
  [db-spec table pk-map raw-map]
  (let [whr (map (fn [[k v]] [:= k v]) pk-map)
        q (-> (h/update table)
              (h/set raw-map))]
    (->> (apply h/where q whr)
         sql/format
         (jdbc/execute! db-spec))))

(def ^:private sqlite-last-id
  (keyword "last_insert_rowid()"))

(defn- update-sqlite-pk [res-map pks]
  (if (= (count pks) 1)
    (assoc res-map (first pks) (sqlite-last-id res-map))
    res-map))

(defn- create!
  "Executes create statement with parameter map."
  [db-spec rsc raw-map opts]
  ;; (prn rsc raw-map)
  (jdbc/insert! db-spec rsc raw-map opts))

(defn create-root
  "Creates root object and attempts to return primary keys. `last_insert_rowid`
  is checked and replaced with first primary key in case of SQLite."
  [params db-con table-key pk-keys]
  (let [opts {:return-keys pk-keys}
        result (first (create! db-con table-key params opts))]
    (if (contains? result sqlite-last-id)
      (update-sqlite-pk result pk-keys)
      result)))
