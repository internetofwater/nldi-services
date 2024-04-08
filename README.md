# Network Linked Data Index Services

[![Spotless Check](https://github.com/internetofwater/nldi-services/actions/workflows/spotless.yml/badge.svg)](https://github.com/internetofwater/nldi-services/actions/workflows/spotless.yml)
[![codecov](https://codecov.io/gh/internetofwater/nldi-services/branch/master/graph/badge.svg)](https://codecov.io/gh/internetofwater/nldi-services)

This repository houses code behind the Network Linked Data Index (NLDI) API [(Swagger Docs)](https://labs.waterdata.usgs.gov/api/nldi/swagger-ui/index.html). The NLDI is hosted as part of the [USGS Waterdata Labs](https://labs.waterdata.usgs.gov/index.html), a set of new capabilities being developed by the USGS Water Mission Area.

## Table of Contents

- [Network Linked Data Index Services](#network-linked-data-index-services)
  - [Table of Contents](#table-of-contents)
  - [Public API](#public-api)
    - [Top Level Path](#top-level-path)
    - [Up/Down Stream Navigation](#updown-stream-navigation)
    - [Up/Down Stream Data](#updown-stream-data)
    - [Query Parameters](#query-parameters)
    - [Other Endpoints](#other-endpoints)
  - [Development](#development)
    - [Dependencies](#dependencies)
    - [Docker Compose](#docker-compose)
    - [Environment Variables](#environment-variables)
    - [Testing](#testing)
    - [Pipelines](#pipelines)
      - [Test](#test)
      - [Build](#build)
      - [Deploy](#deploy)

## Public API

The services are accessed via an HTTP GET request. All output is generated as JSON and GeoJSON.

### Top Level Path

The root path for the NLDI services is `/api/nldi` and follows the hostname under which it is hosted. For example, the QA public endpoint is <https://labs-beta.waterdata.usgs.gov/api/nldi/> and the production endpoint is <https://labs.waterdata.usgs.gov/api/nldi/>. The root path will not return any information. Instead, utilize the sub-paths mentioned in the [Swagger documention](https://labs.waterdata.usgs.gov/api/nldi/swagger-ui/index.html) or the examples below.

The [/api/nldi/linked-data](https://labs.waterdata.usgs.gov/api/nldi/linked-data/) endpoint will give you the valid data source names for the other endpoints. There is also a health check at `/about/health` and version information at `/about/info`.

In general, the API uses hypermedia to help discover options from a given endpoint. A summary of these options follows.

### Up/Down Stream Navigation

/{featureSource}/{featureSourceId}/navigation/{navigationMode} where:

- `{featureSource}` identifies the source used to start navigation:
  - The `comid` `{featureSource}` starts the navigation from an NHDPlus comid
  - any of the network linked feature sources (listed at /)
- `{featureSourceId}` the NHDPlus comid or feature from which to start the navigation
- `{navigationMode}` is the direction and type of navigation:
  - `DD` is `D`ownstream navigation with `D`iversions
  - `DM` is `D`ownstream navigation on the `M`ain channel
  - `PP` is `P`oint to `P`oint navigation (the `stopComid` query parameter is required and must be downstream of the `{comid}`)
  - `UM` is `U`pstream navigation on the `M`ain channel
  - `UT` is `U`pstream navigation including all `T`ributaries

### Up/Down Stream Data

/{featureSource}/{featureSourceId}/navigation/{navigationMode}/{dataSource} where:

- `{featureSource}` identifies the source used to start navigation
- `{featureSourceId}` the NHDPlus comid or other feature from which to start the navigation
- `{navigationMode}` is the direction and type of navigation
- `{dataSource}` is the abbreviation of the data source to return

### Query Parameters

Navigations accept query parameters to further refine/restrict the navigation being requested.

- `f=json` if an html media type is requested explicitely, a blank html page is returned. `f=json` will override this html accept header.
- `distance={dist}` **REQUIRED** limits the navigation to `{dist}` kilometers from the starting point
- `stopComid={stopid}` for use with `PP` navigation between the `{featureSourceId}` and `{stopid}`
  - (only applicable to NHDPlus comid navigation and the `{stopid}` must be downstream of the `{featureSourceId}`)

### Other Endpoints

The NLDI includes additional helper endpoints that will be documented here at a later date. See the [Swagger documentation](https://labs.waterdata.usgs.gov/api/nldi/swagger-ui/index.html) and [NLDI landing page](https://labs.waterdata.usgs.gov/about-nldi/index.html) for more.

## Development

This is a Spring Batch/Boot project.  All of the normal caveats relating to a Spring Batch/Boot application apply.
In general, do not run this project via Docker locally, since it places everything under root ownership.
Rather, start up the demo db and create an application.yml file as described below, then run the project from your IDE.

### Dependencies

This application utilizes a PostgreSQL database.
[nldi-db](https://github.com/internetofwater/nldi-db) contains everything you need to set up a development database environment. It includes data for the Yahara River in Wisconsin.

### Docker Compose

This project includes a Docker-Compose file with all necessary variables predefined.
First, start the demo database by running:

```shell
docker-compose up -d nldi-db
```

Then, build and start the NLDI services by running:

```shell
docker-compose up nldi-services
```

These test services will be accessible at <localhost:8080/nldi>. \
If you would like to build these images using a mirror url simply set a `DOCKER_MIRROR` environment variable or include
it as a build argument.

### Environment Variables

To run the project (connecting to a separately running db instance) you will need to create the file application.yml in the project's root directory and add the following (normal defaults are filled in):

```yaml
nldiDbHost: localhost
nldiDbPort: 5437 #Or whatever port you map it to
nldiDbUsername: dbUserName #See nldi-db project .env file 'NLDI_READ_ONLY_USERNAME'
nldiDbPassword: dbPassword #See nldi-db project .env file 'NLDI_READ_ONLY_PASSWORD'
nldiDbName: dbName #See nldi-db project .env file 'NLDI_DATABASE_NAME'
nldiProtocol: http
nldiHost: owi-test.usgs.gov:8080
nldiPath: /test-url
pygeoapiUrl: https://labs-beta.waterdata.usgs.gov/api/nldi/pygeoapi/

serverContextPath: /nldi
springFrameworkLogLevel: INFO
serverPort: 8080

spring.security.user.password: changeMe
```

### Testing

This project contains JUnit tests. Maven can be used to run them (in addition to the capabilities of your IDE).

To run the unit tests of the application use:

```shell
mvn test
```

To additionally start up a Docker database and run the integration tests of the application use:

```shell
mvn verify
```

### Pipelines

This project has automated pipelines for test, build, and deploy.

#### Test

The test pipeline is run when a pull request is created and consists of two parts. The first step is linting using the Spotless Maven plugin. This validates that any code changes fit within certain style guidelines to maintain readability and consistency. The second stage runs all unit and integration tests. If both of these stages pass, a pull request is eligible to be merged.

#### Build

The build pipeline happens internally to the USGS and are triggered manually. The `docker build` command is run to construct a Docker container after which it is pushed to an internal container registry.

#### Deploy

Similar to the build pipeline, deploys are internal and triggered manually. The infrastructure components are managed with this pipeline and retrieve the Docker container from the previously mentioned container registry.
