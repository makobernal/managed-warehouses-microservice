#Managed Warehouses μ-service base docker image

FROM java:7

MAINTAINER makobernal@gmail.com

# Stockservice jar
ADD ./target/managed-warehouses-0.1.0-SNAPSHOT-standalone.jar /root/warehouses-service.jar

ENV STOCK_DB_SUBPROTOCOL mysql
ENV STOCK_DB_SUBNAME //localhost:3306/stock
ENV STOCK_DB_USER root
ENV STOCK_DB_PASSWORD

# Default entrypoint
CMD ["java","-jar", "warehouses-service.jar"]
EXPOSE 3000
