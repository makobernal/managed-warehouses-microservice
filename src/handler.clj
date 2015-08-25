(ns handler
      (:use compojure.core)
      (:use cheshire.core)
      (:use ring.util.response)
      (:require [compojure.handler :as handler]
                [ring.middleware.json :as middleware]
                [compojure.route :as route]
                [core :as core]))



(defroutes app-routes

  (context "/status" []
    (GET   "/" [] (core/show-status-page))
    (GET   "/test" [] (core/rebuild-database)))

  (context  "/warehouses" []
    (GET    "/" {params :params} (core/get-warehouses params))
    (GET    "/:wh_id" [wh_id] (core/get-warehouse-by-id wh_id))
    (POST   "/" {body :body} (core/create-warehouse body))
    (PUT    "/:wh_id" {body :body params :params} (core/update-warehouse (:wh_id params) (:name body)))
    (DELETE "/:wh_id" [wh_id] (core/delete-warehouse wh_id))

    (context  "/:wh_id/stockitems" [wh_id]
      (GET    "/" {params :params} (core/get-stockitems-by-warehouse wh_id (:skus params)))
      (GET    "/:sku" [sku] (core/get-stockitem wh_id sku))
      (PUT    "/:sku" {body :body params :params} (core/update-or-insert-stockitem wh_id (:sku params) (:qty body)))
      (DELETE "/:sku" [sku] (core/delete-stockitem wh_id sku))

      (context "/:sku/changes" [sku]
        (POST "/" {body :body} (core/stockitem-change wh_id sku (:qty body))))))

  (route/not-found "Route not Found"))


(def app
    (-> (handler/api app-routes)
        (middleware/wrap-json-body {:keywords? true})
        (middleware/wrap-json-response)))
