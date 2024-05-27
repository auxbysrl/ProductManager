# Auxby Products Manager Microservice.

Handle clients operation to get specific details for products available for sale/auction, or other details
required to support all Auxby features related to products.


![Coverage](badges/jacoco.svg) ![Branches](badges/branches.svg)


# Compile and Build

    mvn clean install

# Configuration

* Database configuration
    * ${DB_HOST} - the database url
    * ${DB_PORT} - the database port
    * ${DB_NAME} - the database name
    * ${DB_USER} - the database username
    * ${DB_PASSWORD} - the database password
    * ${PRODUCT_DOMAIN_NAME} - the domain name (e.g: localhost)
    * ${EUREKA_URL} - the eureka URL
    * ${EUREKA_USER_PASSWORD} - password used by eureka

# Deployment

* From Heroku select the branch and click deploy branch

# Deploy on PROD
- connect to VPS server using **SSH**
- on local machine go to project root file and build jar file using **_mvn clean package_** ( before building the jar pay attention to variables set on application.yaml/application-local.yaml to connect to DB, RabbitMQ, SSL, etc.)
- check service stutus running **_sudo systemctl status auxby-product.service_**
- stop the service **_sudo systemctl stop auxby-product.service_**
- copy/replace the server jar file from **_/home/auxby-platform#_**
- run the service using **_sudo systemctl start auxby-platform.service_**
