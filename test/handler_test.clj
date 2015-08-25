(ns handler_test
  (:use clojure.test
        ring.mock.request
        cheshire.core
        handler)
  (:require [db :as db]))

;; Tests Util functions
(defn create-test-database []
  (db/rebuild-database)
  (db/add-warehouse "London Supply Centre" "Unnamed Fashion Retalier")
  (db/add-warehouse "Paris Supply Centre" "Unnamed Fashion Retalier")
  (db/add-warehouse "UK Warehouse" "Big Food Chain")
  (db/insert-stockitem "SKU-1" 1 20)
  (db/insert-stockitem "SKU-2" 1 33)
  (db/insert-stockitem "SKU-3" 1 25)
  (db/insert-stockitem "XYZ-123" 2 15)
  (db/insert-stockitem "ABC-456" 2 20))

(defn teardown-test-database []
  (db/rebuild-database))

(def not-nil? (complement nil?))

(defn is-warehouse? "Checks the parameter is a map with the appropiate fields of a warehouse"
  [warehouse] (and (not-nil? (:tenant warehouse)) (not-nil? (:name warehouse)) (not-nil? (:id warehouse))))

(defn is-stockitem? "Checks the parameter is a map with the appropiate fields of a stockitem"
  [stockitem] (and (not-nil? (:sku stockitem)) (not-nil? (:wh_id stockitem)) (not-nil? (:qty stockitem))))

;; Actual Tests Suites
(deftest app-routes-test

  (testing "not-found routes"
    (let [response (app (request :get "/invalid"))]
      (is (= (:status response) 404))))

  (testing "status route"
    (let [response (app (request :get "/status"))]
      (is (= (:status response) 200))
      (is (not (empty? (:body response) ))))))

(deftest warehouse-routes

  (create-test-database)

  (testing "GET    /warehouses/"
    (let [response (app (request :get "/warehouses"))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (not (empty? body)))
      (is (is-warehouse? (first body)))))

  (testing "GET    /warehouses/?tenant="
    (let [response (app (request :get "/warehouses" {:tenant "Big Food Chain"}))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 1 (count body)))  ;;Check that we only received one warehouse result
      (is (= (:tenant (first body)) "Big Food Chain"))))

  (testing "GET    /warehouses/wh_id"
    (let [response (app (request :get "/warehouses/2"))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (is-warehouse? body))))

  (testing "POST   /warehouses/"
    (let [response (app
                    (body (content-type (request :post "/warehouses") "application/json")
                          (generate-string {:name "WH" :tenant "Big Food Chain"})))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (not-nil? (db/extract-gen-key (first body)))))) ;;Check that an id has been generated

  (testing "PUT    /warehouses/wh_id"
    (let [response (app
                    (body (content-type (request :put "/warehouses/4") "application/json")
                          (generate-string {:name "WHA" :tenant "Big Food Chain"})))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 1 (first body))))) ;;Check that one record has been updated

  (testing "DELETE /warehouses/wh_id"
    (let [response (app (request :delete "/warehouses/4"))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 1 (first body))))) ;;Check that one record has been deleted

  (teardown-test-database))

(deftest stockitems-routes

  (create-test-database)

  (testing "GET    /warehouses/wh_id/stockitems"
    (let [response (app (request :get "/warehouses/1/stockitems"))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (not (empty? body)))
      (is (is-stockitem? (first body)))))

  (testing "GET    /warehouses/wh_id/stockitems?skus="
    (let [response (app (request :get "/warehouses/1/stockitems" {:skus "SKU-1,SKU-2"}))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 2 (count body)))
      (is (= (:sku (first body)) "SKU-1"))))

  (testing "GET    /warehouses/wh_id/stockitems/sku"
    (let [response (app (request :get "/warehouses/2/stockitems/XYZ-123"))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (is-stockitem? body))))

  (testing "PUT    /warehouses/wh_id/stockitems/sku"
    (let [response (app (body
                         (content-type (request :put "/warehouses/1/stockitems/SKU-1") "application/json")
                       (generate-string {:qty 2})))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 1 (first body))))) ;;Check that one record has been updated

  (testing "DELETE /warehouses/wh_id/stockitems/sku"
    (let [response (app (request :delete "/warehouses/1/stockitems/SKU-1"))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 1 (first body))))) ;;Check that one record has been deleted

  (testing "POST   /warehouses/wh_id/stockitems/sku/changes"
    (let [response (app (body
                         (content-type (request :post "/warehouses/2/stockitems/XYZ-123/changes") "application/json")
                       (generate-string {:qty 25})))
          body (parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= 1 (first body)))))

  (teardown-test-database))
