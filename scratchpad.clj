(ns db_test
  (:use db
        clojure.test
        ring.util.response)
  (:require [clojure.java.jdbc :as sql]
            [core :as core]
            [handler :as handler]
            [clojure.string :as str]))


(rebuild-database)

(sql/insert! (db-connection) :warehouse [:name :tenant] ["Warehouse One" "Elche Group"])
(sql/insert! (db-connection) :warehouse [:name :tenant] ["Warehouse Two" "Elche Group"])
(sql/insert! (db-connection) :warehouse [:name :tenant] ["Warehouse One" "Albacete Conglomerate"])
(sql/insert! (db-connection) :warehouse [:name :tenant] ["Warehouse Two" "Albacete Conglomerate"])
(sql/insert! (db-connection) :warehouse [:name :tenant] ["Warehouse A" "Elche Group"])
(sql/insert! (db-connection) :stockitem [:sku :wh_id :qty] ["mysku" 1 1])
(sql/insert! (db-connection) :stockitem [:sku :wh_id :qty] ["mysku" 2 345])
(sql/insert! (db-connection) :stockitem [:sku :wh_id :qty] ["mysku2" 2 220])


;;This way is better because we get the :generated_key in the result
(sql/insert! (db-connection) :warehouse {:name "Warehouse 13" :tenant "Albacete Conglomerate"})

(get-warehouses-by-tenant "Elche Group")
(get-warehouses-by-tenant "Albacete Conglomerate")


(count (get-all-warehouses))
(get-warehouses-by-tenant "Albacete Conglomerate")

(:generated_key (first (add-warehouse "Warehouse Oen" "MyTenant")))
(get-warehouses-by-tenant "MyTenant")
(update-warehouse-name "Warehouse One" "MyTenant")



(let [generated-id (:generated_key (first (add-warehouse "Warehouse OXY" "MyTenant")))]
  (not(empty? (get-all-warehouses)))
  (update-warehouse-name generated-id "WarehOUSE BALX")
  )


(insert-stockitem "skuxyz" 1 34)
(get-stockitems-by-warehouse 1)
(get-stockitem-by-warehouse-and-sku 1 "mysku")
(update-stockitem-amount "mysku" 1 99)
(get-stockitem-by-warehouse-and-sku 1 "mysku")
(increase-stockitem-amount "mysku" 1 1)
(get-stockitem-by-warehouse-and-sku 1 "mysku")

(def skus '("abc123" "abc456" "abc789"))
skus

(get-stockitems-by-warehouse-and-sku-list 1 skus)

(get-stockitem-by-warehouse-and-sku 1 "skuxyz")

(response (get-all-warehouses))

(core/get-all-warehouses {:tenant "Albacete Conglomerate"})


(core/update-or-insert-stockitem 1 "abdc" 20)
;; /warehouses GET
;; /warehouses?tenant_id=1 GET
;; /warehouses POST
;; /warehouses/:warehouse_id PUT
;; /warehouses/:warehouse_id DELETE


;; /warehouses/:warehouse_id/stockitem s GET
;; /warehouses/:warehouse_id/stockitems?skus=6464,57647,5675 GET
;; /warehouses/:warehouse_id/stockitems PATCH

;; /warehouses/:warehouse_id/stockitems/:sku GET
;; /warehouses/:warehouse_id/stockitems/:sku    PUT
;; /warehouses/:warehouse_id/stockitems/:sku    DELETE

;; /warehouses/:warehouse_id/stockitems/:sku/changes/    POST
