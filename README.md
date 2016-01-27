[![Build Status](https://travis-ci.org/trustedanalytics/service-catalog.svg?branch=master)](https://travis-ci.org/trustedanalytics/service-catalog)

service-catalog
===============

### Running locally
To run the service locally or in Cloud Foundry, the following environment variables need to be defined:

* `VCAP_SERVICES_SSO_CREDENTIALS_APIENDPOINT` - a Cloud Foundry API endpoint;
* `VCAP_SERVICES_SSO_CREDENTIALS_TOKENKEY` - an UAA endpoint for verifying token signatures;

Starting the service:

* `mvn clean spring-boot:run`

### Documentation
After starting a local instance, it's available at http://localhost:8080/sdoc.jsp
