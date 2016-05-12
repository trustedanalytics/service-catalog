[![Build Status](https://travis-ci.org/trustedanalytics/service-catalog.svg?branch=master)](https://travis-ci.org/trustedanalytics/service-catalog)
[![Dependency Status](https://www.versioneye.com/user/projects/57236ffaba37ce0031fc1f38/badge.svg?style=flat)](https://www.versioneye.com/user/projects/57236ffaba37ce0031fc1f38)

service-catalog
===============

### Running locally
To run the service locally or in Cloud Foundry, the following environment variables need to be defined:

* `VCAP_SERVICES_SSO_CREDENTIALS_APIENDPOINT` - a Cloud Foundry API endpoint;
* `VCAP_SERVICES_SSO_CREDENTIALS_TOKENKEY` - an UAA endpoint for verifying token signatures;

Note that the user provided service named `marketplace-register-service` needs to be configured when running locally.
You need to set `applicationBrokerUrl`, `username` and `password` variables. In order to do this you may either edit `broker` section in `application.yml` file (which is currently preconfigured with default values) or you can add the following object to `user-provided` list in your local VCAP_SERVICES environment variable:

```
{
  "credentials": {
    "applicationBrokerUrl": "<application-broker-url>",
    "password": "<application-broker-pass>",
    "username": "<application-broker-user>"
  },
  "label": "user-provided",
  "name": "marketplace-register-service",
  "syslog_drain_url": "",
  "tags": []
}
```

After editing VCAP_SERVICES environment variable, copy it to clipboard and run:
```
export VCAP_SERVICES='<modified_vcap_services>'
```

Starting the service:

* `mvn clean spring-boot:run`

### Documentation
After starting a local instance, it's available at http://localhost:8080/sdoc.jsp
