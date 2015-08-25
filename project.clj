(defproject managed-warehouses "0.1.0-SNAPSHOT"
  :description "Managed Warehouses Service"
  :url "https://github.com/makobernal/managed-warehouses-microservice"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring/ring-json "0.2.0"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [com.taoensso/timbre "3.1.1"]
                 [sonian/carica "1.0.4" :exclusions [[cheshire]]]
                 [environ "0.4.0"]]
  :plugins [[lein-ring "0.8.10"]
            [lein-environ "0.4.0"]]
  :ring {:handler app}
  :profiles {:dev {:dependencies [[com.h2database/h2 "1.3.174"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]
                   :env {:stock-db-subprotocol "h2"
                         :stock-db-subname "mem:stock"
                         :stock-db-user ""
                         :stock-db-password ""}}
             :prod {:env {:stock-db-subprotocol "mysql"
                         :stock-db-subname "//localhost:3306/stock"
                         :stock-db-user "root"
                         :stock-db-password ""}}})
