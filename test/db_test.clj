(ns db_test
  (:use clojure.test
        db))

(deftest test-warehouse-crud-operations

  (rebuild-database)

  (testing "I can create a warehouse, I can query it, update it and delete it"

    (let [generated-id (extract-gen-key (first (add-warehouse "Warehouse Oen" "MyTenant")))]

    (is (not (empty? (get-all-warehouses))))
    (is (= (:name (get-warehouse-by-id generated-id) "Warehouse Oen")))

    (update-warehouse-name generated-id "Warehouse One")
    (is (= (:name (get-warehouse-by-id generated-id) "Warehouse One")))

    (delete-warehouse-by-id generated-id)
    (is (empty? (get-all-warehouses))))))

(deftest test-warehouse-constraints

  (rebuild-database)

  (testing "I can't add a warehouse without name or tenant"
    (is (thrown? Exception (add-warehouse nil nil))))


  (testing "I can't add a warehouse with the same name in the same tenant"
    (add-warehouse "name" "tenant")
    (is (thrown? Exception (add-warehouse "name" "tenant")))
    (add-warehouse "name2" "tenant")))


(deftest test-stockitems-crud-operations

  (rebuild-database)

  (let [wh_id (extract-gen-key (first (add-warehouse "Sample Warehouse" "Sample Tenant")))]

  (testing "I can add, query and delete a stockitem to an existing warehouse"
    (insert-stockitem "abc123" wh_id 0)
    (is (< 0 (count (get-one-stockitem-by-warehouse-and-sku wh_id "abc123"))))
    (delete-stockitem-from-warehouse "abc123" wh_id)
    (is (empty? (get-one-stockitem-by-warehouse-and-sku wh_id "abc123"))))

  (testing "I can update a stockitem's quantity")
    (insert-stockitem "abc456" wh_id 0)
    (update-stockitem-amount "abc456" wh_id 12)
    (is (= 12 (:qty (get-one-stockitem-by-warehouse-and-sku wh_id "abc456"))))


  (testing "I can increase or decrease a stockitem's quantity")
    (insert-stockitem "abc789" wh_id 20)
    (increase-stockitem-amount "abc789" wh_id 5)
    (is (= 25 (:qty (get-one-stockitem-by-warehouse-and-sku wh_id "abc789"))))
    (increase-stockitem-amount "abc789" wh_id -10)
    (is (= 15 (:qty (get-one-stockitem-by-warehouse-and-sku wh_id "abc789"))))

  (testing "I can query multilple stockitems"
    (let [skus '("abc123" "abc456" "abc789")]
      (is (= 2 (count(get-stockitems-by-warehouse-and-sku-list wh_id skus))))))))

(deftest test-stockitems-constraints

  (rebuild-database)

  (testing "I can't add a stockitem if the warehouse doesn't exist"
    (is (thrown? Exception (insert-stockitem "abc456" 100 0))))

  (testing "I can't add a SKU more than once to a warehouse"
    (let [wh_id (extract-gen-key (first (add-warehouse "Sample Warehouse" "Sample Tenant")))]
      (insert-stockitem "abc789" wh_id 20)
      (is (thrown? Exception (insert-stockitem "abc789" wh_id 20)))
      (insert-stockitem "xyz789" wh_id 20))))
