# managed-warehouses-microservice

A simple clojure based μ-service app with a REST interface to create tenants which manage warehouses where stock items with quantity can be added.

The app uses a simple in-memory database but a MYSQL db can be plugged and should be compatible.

## Running the app

./lein ring server

## Usage

/warehouses GET
/warehouses?tenant_id=1 GET
/warehouses POST
/warehouses/:warehouse_id PUT
/warehouses/:warehouse_id DELETE

/warehouses/:warehouse_id/stockitem s GET
/warehouses/:warehouse_id/stockitems?skus=6464,57647,5675 GET
/warehouses/:warehouse_id/stockitems PATCH

/warehouses/:warehouse_id/stockitems/:sku GET
/warehouses/:warehouse_id/stockitems/:sku    PUT
/warehouses/:warehouse_id/stockitems/:sku    DELETE

/warehouses/:warehouse_id/stockitems/:sku/changes/    POST
