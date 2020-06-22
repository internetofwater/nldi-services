# NLDI REST (like) Services

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f0153ed6b07340bda3c04d6f05df6e8c)](https://app.codacy.com/app/usgs_wma_dev/nldi-services?utm_source=github.com&utm_medium=referral&utm_content=ACWI-SSWD/nldi-services&utm_campaign=Badge_Grade_Settings)
[![Build Status](https://travis-ci.org/ACWI-SSWD/nldi-services.svg?branch=master)](https://travis-ci.org/ACWI-SSWD/nldi-services)

## Public API
The services are accessed via an http GET request. All navigation output is generated as GeoJSON ("application/vnd.geo+json")

### The root is {host}/nldi.
Both a test and a production are exposed to the public:
__https://labs-beta.waterdata.usgs.gov/nldi/linked_data__ is the test root.
__https://labs.waterdata.usgs.gov/nldi/linked_data__ is the production root.
This endpoint will give you the valid dataSource names for the other endpoints. There is also a health check at /about/health and version information at /about/info.

### Display Up/Down Stream Flow Lines
/{featureSource}/{featureSourceId}/navigate/{navigationMode} where:
*   __{featureSource}__ identifies the source used to start navigation:
*   __comid__ start the navigation from an NHDPlus comid
*   any of the network linked feature sources (listed at /)
*   __{featureSourceId}__ the NHDPlus comid or feature from which to start the navigation
*   __{navigationMode}__ is the direction and type of navigation:
*   __DD__ is __D__ownstream navigation with __D__iversions
*   __DM__ is __D__ownstream navigation on the __M__ain channel
*   __PP__ is __P__oint to __P__oint navigation (the __stopComid__ query parameter is required and must be downstream of the __{comid}__)
*   __UM__ is __U__pstream navigation on the __M__ain channel
*   __UT__ is __U__pstream navigation including all __T__ributaries

### Display Up/Down Stream Events
/{featureSource}/{featureSourceId}/navigate/{navigationMode}/{dataSource} where:
*   __{featureSource}__ identifies the source used to start navigation  (same values as for Flow Lines)
*   __{featureSourceId}__ the NHDPlus comid or other feature from which to start the navigation
*   __{navigationMode}__ is the direction and type of navigation (same values as for Flow Lines)
*   __{dataSource}__ is the abbreviation of the data source from which events should be shown

### Query Parameters
Both endpoints accept the same query parameters to further refine/restrict the navigation being requested
*   __distance={dist}__ limit the navigation to __{dist}__ kilometers from the starting point
*   __stopComid={stopid}__ for use with __PP__ navigation between the __{featureSourceId}__ and __{stopid}__
*   (only applicable to NHDPlus comid navigation and the __{stopid}__ must be downstream of the __{featureSourceId}__)

## Development
This is a Spring Batch/Boot project.  All of the normal caveats relating to a Spring Batch/Boot application apply.
In general, do not run this project via Docker locally, since it places everything under root ownership.
Rather, start up the demo db and create an application.yml file as described below, then run the project from your IDE.

### Dependencies
This application utilizes a PostgreSQL database.
[nldi-db](https://github.com/ACWI-SSWD/nldi-db) contains everything you need to set up a development database environment. It includes data for the Yahara River in Wisconsin.

### Running the Demo DB for local development
See the nldi-db project for more details, but in short:
```shell
docker network create --subnet=172.26.0.0/16 nldi
docker run -it --env-file ./.env -p 127.0.0.1:5437:5432/tcp usgswma/nldi-db:demo
```
Note the _5437_ port mapping, which is used in the environmental variables below.

### Environment variables
To run the project (connecting to a separately running db instance) you will need to create the file application.yml in the project's root directory and add the following (normal defaults are filled in):
```yaml
nldiDbHost: localhost
nldiDbPort: 5437 #Or whatever port you map it to
nldiDbUsername: [dbUserName] #See nldi-db project .env file 'NLDI_READ_ONLY_USERNAME'
nldiDbPassword: [dbPassword] #See nldi-db project .env file 'NLDI_READ_ONLY_PASSWORD'

nldiProtocol: http
nldiHost: owi-test.usgs.gov:8080
nldiPath: /test-url

serverContextPath: /nldi
springFrameworkLogLevel: INFO
serverPort: 8080

spring.security.user.password: changeMe
```

### Testing
This project contains JUnit tests. Maven can be used to run them (in addition to the capabilities of your IDE).

To run the unit tests of the application use:

```shell
mvn package
```

To additionally start up a Docker database and run the integration tests of the application use:

```shell
mvn verify -DTESTING_DATABASE_PORT=5445 -DTESTING_DATABASE_ADDRESS=localhost -DTESTING_DATABASE_NETWORK=nldi
```
