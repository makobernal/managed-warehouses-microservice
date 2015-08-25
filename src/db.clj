(ns db
      (:import com.mchange.v2.c3p0.ComboPooledDataSource)
      (:require [clojure.java.jdbc :as sql]
                [environ.core :as env]
                [clojure.string :as str]))

(def db-spec {:subprotocol (env/env :stock-db-subprotocol)
              :subname (env/env :stock-db-subname)
              :user (env/env :stock-db-user)
              :password (env/env :stock-db-password)})

(println db-spec)

(defn ish2db? []
   (= "h2" (:subprotocol db-spec)))

(defn ismysqldb? []
   (= "mysql" (:subprotocol db-spec)))

(defn which-driver []
  (if (ish2db?) "org.h2.Driver"
      "com.mysql.jdbc.Driver"))

(defn pool
  [spec]
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass (which-driver))
               (.setJdbcUrl (str "jdbc:" (:subprotocol spec) ":" (:subname spec)))
               (.setUser (:user spec))
               (.setPassword (:password spec))
               (.setMaxPoolSize 1)
               (.setMinPoolSize 1)
               (.setInitialPoolSize 1)
               ; expire excess connections after 30 minutes of inactivity:
               (.setMaxIdleTimeExcessConnections (* 30 60))
               ; expire connections after 3 hours of inactivity:
               (.setMaxIdleTime (* 3 60 60)))]
    {:datasource cpds}))


(def pooled-db (delay (pool db-spec)))

(defn db-connection [] @pooled-db)

(defn extract-gen-key "extracts the generated id from SQL inserts, taking into account the DB dialect used" [returnmap]
  (if (ish2db?)
   ((keyword "scope_identity()") returnmap)
   (:generated_key returnmap)))

;; Database creation and building operations

(defn create-warehouse-table []
    (sql/create-table-ddl :warehouse [:id "integer" "primary key" "auto_increment"]
                                 [:name "varchar(256)" "not null"]
                                 [:tenant "varchar(256)" "not null"]
                                 ["unique (name,tenant)"]))

(defn create-stockitem-table []
    (sql/create-table-ddl :stockitem [:sku "varchar(256)"]
                                 [:wh_id "integer" ]
                                 [:qty "integer" "not null"]
                                 ["primary key" "(sku,wh_id)"]
                                 ["foreign key (wh_id) references warehouse (id)"]))


(defn rebuild-database "Drops existing objects in configured database and recreates the required db structure"[]
  (try (sql/db-do-commands (db-connection)
        (sql/drop-table-ddl :stockitem)
        (sql/drop-table-ddl :warehouse)) (catch Exception _))

  (sql/db-do-commands (db-connection)
    (create-warehouse-table)
    (create-stockitem-table)))

;; This will create the db on startup
(rebuild-database)

;; Warehouse CRUD operations

(defn get-all-warehouses []
  (sql/query (db-connection) ["SELECT * FROM warehouse"]))

(defn get-warehouse-by-id [id]
  (first (sql/query (db-connection) ["SELECT * FROM warehouse WHERE id = ?" id])))

(defn get-warehouses-by-tenant [tenant]
  (sql/query (db-connection) ["SELECT * FROM warehouse WHERE tenant = ? ORDER BY name" tenant]))

(defn add-warehouse [name tenant]
  (sql/insert! (db-connection) :warehouse {:name name :tenant tenant}))

(defn update-warehouse-name [id name]
  (sql/update! (db-connection) :warehouse {:name name} ["id = ?" id]))

(defn delete-warehouse-by-id [id]
  (sql/delete! (db-connection) :warehouse ["id = ?" id]))

;;StockItems operations

(defn get-stockitems-by-warehouse [wh_id]
  (sql/query (db-connection) ["SELECT * FROM stockitem WHERE wh_id = ?" wh_id]))

(defn get-stockitems-by-warehouse-and-sku-list [wh_id skus]
  (sql/query (db-connection)
    [(str "SELECT * FROM stockitem WHERE wh_id = ? AND sku IN ('" (str/join "','" skus) "')") wh_id]))

(defn get-one-stockitem-by-warehouse-and-sku [wh_id sku]
  (first (sql/query (db-connection) ["SELECT * FROM stockitem WHERE wh_id = ? AND sku = ?" wh_id sku])))

(defn insert-stockitem [sku wh_id qty]
  (sql/insert! (db-connection) :stockitem {:sku sku :wh_id wh_id :qty qty}))

(defn update-stockitem-amount [sku wh_id qty]
  (sql/update! (db-connection) :stockitem {:qty qty} ["sku = ? AND wh_id = ?" sku wh_id]))

(defn increase-stockitem-amount [sku wh_id amt]
  (sql/execute! (db-connection) ["UPDATE stockitem SET qty = qty + ? WHERE sku = ? AND wh_id = ?" amt sku wh_id]))

(defn delete-stockitem-from-warehouse [sku wh_id]
  (sql/delete! (db-connection) :stockitem ["sku = ? AND wh_id = ?" sku wh_id]))

;;Metrics queries

(defn how-many-warehouses []
  (sql/query (db-connection) ["SELECT COUNT(*) as warehousecount FROM warehouse"]))
(defn how-many-tenants []
  (sql/query (db-connection) ["SELECT COUNT(distinct(tenant)) as tenantcount FROM warehouse"]))
(defn how-many-stockitems []
  (sql/query (db-connection) ["SELECT COUNT(*) as stockitemcount FROM stockitem"]))
