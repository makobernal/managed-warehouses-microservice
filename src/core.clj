(ns core
  (:use ring.util.response)
  (:require [db :as db]
            [clojure.string :as str]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; Utils
(defn notfound-response [body]
  (status (response body) 404))

;;Status Page Controller

(defn show-status-page
  "Status can be used to check the status of the server"
  []
  (conj (db/how-many-tenants) (db/how-many-warehouses)(db/how-many-stockitems)))

(defn rebuild-database [] (db/rebuild-database))

;; Warehouses CRUD Controllers

(defn get-warehouses [params]
  (if (empty? (:tenant params))
    (response (db/get-all-warehouses))
    (response (db/get-warehouses-by-tenant (:tenant params)))))

(defn get-warehouse-by-id [id]
  (let [dbresponse (db/get-warehouse-by-id id)]
    (if (empty? dbresponse)
      (notfound-response nil)
      (response dbresponse))))

(defn create-warehouse [body]
  (response (db/add-warehouse (:name body) (:tenant body))))

(defn update-warehouse [id name]
  (response (db/update-warehouse-name id name)))

(defn delete-warehouse [id]
  (response (db/delete-warehouse-by-id id)))


;; Stockitems operations Controllers

(defn get-stockitems-by-warehouse [wh_id skus]
  (info "fetching stockitems from warehouse" wh_id)
  (if (empty? skus)
    (response (db/get-stockitems-by-warehouse wh_id))
    (let [skulist (str/split skus #",")]
      (response (db/get-stockitems-by-warehouse-and-sku-list wh_id skulist)))))

(defn get-stockitem [wh_id sku]
  (info "getting stockitem" sku "from warehouse" wh_id)
  (let [dbresponse (db/get-one-stockitem-by-warehouse-and-sku wh_id sku)]
    (if (empty? dbresponse)
      (notfound-response nil)
      (response dbresponse))))

(defn update-or-insert-stockitem [wh_id sku quantity]
  (info "updating or inserting sku " sku "in warehouse" wh_id)
  (let [result (db/update-stockitem-amount sku wh_id quantity)]
        (if (zero? (first result))
          (response (db/insert-stockitem sku wh_id quantity))
          (response result))))

(defn delete-stockitem [wh_id sku]
  (response (db/delete-stockitem-from-warehouse sku wh_id)))

(defn stockitem-change [wh_id sku quantity]
  (info "increasing/decreasing quantity for " sku "in warehouse" wh_id)
  (response (db/increase-stockitem-amount sku wh_id quantity)))
